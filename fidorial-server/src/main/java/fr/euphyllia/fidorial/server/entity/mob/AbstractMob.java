package fr.euphyllia.fidorial.server.entity.mob;

import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.inventory.PlayerInventories;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundEntityPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.entity.mob.MobBehaviour;
import fr.fidorial.entity.player.Player;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public abstract class AbstractMob extends AbstractLivingEntity {

    private static final byte ENTITY_EVENT_DEATH = 3;

    /**
     * For how long a mob remembers what hurt it, as in Vanilla.
     */
    private static final int DAMAGE_MEMORY_TICKS = 40;
    /**
     * For how long a mob counts as killed by the player who hit it, as in Vanilla.
     */
    private static final int HURT_BY_PLAYER_MEMORY_TICKS = 100;

    private @Nullable DamageSource lastDamageSource;
    private int damageMemoryTicks;
    private int hurtByPlayerTicks;

    private final List<MobBehaviour> behaviours = new CopyOnWriteArrayList<>();

    protected AbstractMob(final int entityId, final UUID uuid, final EntityType type, final Location location, final float maxHealth) {
        super(entityId, uuid, type, location, maxHealth);
    }

    @Override
    public final void setHealth(final float health) {
        super.setHealth(health);
        if (health() <= 0f && !isDying()) {
            onDeath();
        }
    }

    protected void onDeath() {
        server().debug().gameEvents().emit(world(), GameEventKeys.ENTITY_DIE, location());
        sendToTrackers(new ClientboundEntityEventPacket(entityId(), ENTITY_EVENT_DEATH));
        startDeathAnimation();
        if (shouldDropLoot()) {
            dropLoot();
        }
        dropExperience();
        dispatch(MobBehaviour::onDeath, "onDeath");
    }

    /**
     * Leaves the experience of this mob where it died, as vanilla does: only if a player hit it
     * within the last five seconds, never for a baby, and not while the {@code mob_drops} game rule
     * is off.
     */
    private void dropExperience() {
        if (hurtByPlayerTicks <= 0 || !shouldDropExperience()
                || !(world() instanceof final ServerWorld serverWorld)
                || !serverWorld.gameRuleValues().getBoolean(GameRuleKeys.MOB_DROPS)) {
            return;
        }
        final Location at = location();
        server().entities().awardExperience(serverWorld, at.x(), at.y(), at.z(), experienceReward());
    }

    /**
     * {@return {@code true} if this mob leaves experience when a player kills it}: as in Vanilla, a
     * baby leaves none
     */
    protected boolean shouldDropExperience() {
        return !isBaby();
    }

    /**
     * {@return how much experience this mob leaves when a player kills it}
     */
    protected int experienceReward() {
        return 0;
    }

    /**
     * {@return {@code true} if a player hit this mob within the last five seconds}: it then counts
     * as killed by a player
     */
    public final boolean wasRecentlyHurtByPlayer() {
        return hurtByPlayerTicks > 0;
    }

    /**
     * {@return {@code true} if this mob leaves its loot when it dies}: as in Vanilla, babies leave
     * nothing, and nothing drops while the {@code mob_drops} game rule is off
     */
    protected boolean shouldDropLoot() {
        return !isBaby()
                && world() instanceof final ServerWorld serverWorld
                && serverWorld.gameRuleValues().getBoolean(GameRuleKeys.MOB_DROPS);
    }

    /**
     * Drops what this mob leaves when it dies.
     */
    protected void dropLoot() {
    }

    /**
     * Drops a stack where this mob stands, as vanilla drops the loot of a mob.
     */
    protected final void dropLootItem(final ItemStack stack) {
        if (stack.isEmpty() || !(world() instanceof final ServerWorld serverWorld)) {
            return;
        }
        final Location at = location();
        server().entities().dropItem(serverWorld, at.x(), at.y(), at.z(), stack,
                0.0, 0.0, 0.0, ItemEntity.DEFAULT_PICKUP_DELAY, null);
    }

    /**
     * {@return {@code true} if this mob is a baby}
     */
    public boolean isBaby() {
        return false;
    }

    /**
     * {@return what last hurt this mob, {@code null} once {@value #DAMAGE_MEMORY_TICKS} ticks have
     * passed}
     */
    public final @Nullable DamageSource lastDamageSource() {
        return damageMemoryTicks > 0 ? lastDamageSource : null;
    }

    /**
     * Remembers what just hurt this mob.
     */
    public final void rememberDamage(final DamageSource source) {
        this.lastDamageSource = source;
        this.damageMemoryTicks = DAMAGE_MEMORY_TICKS;
        if (source.causingEntity() instanceof Player) {
            this.hurtByPlayerTicks = HURT_BY_PLAYER_MEMORY_TICKS;
        }
    }

    /**
     * Forgets what hurt this mob once it is old enough; called once per tick.
     */
    protected final void tickDamageMemory() {
        if (damageMemoryTicks > 0 && --damageMemoryTicks == 0) {
            lastDamageSource = null;
        }
        if (hurtByPlayerTicks > 0) {
            hurtByPlayerTicks--;
        }
    }

    /**
     * Writes what this kind of mob keeps across restarts, under the names vanilla saves it.
     *
     * @param tag the entity being written
     */
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
    }

    /**
     * Reads back what {@link #saveAdditional(CompoundBinaryTag.Builder)} wrote.
     *
     * @param tag the saved entity
     */
    public void loadAdditional(final CompoundBinaryTag tag) {
    }

    @Override
    protected void onRemoved() {
        dispatch(MobBehaviour::onRemove, "onRemove");
    }

    protected Sound.@Nullable Type hurtSound() {
        return null;
    }

    protected Sound.@Nullable Type deathSound() {
        return null;
    }

    /**
     * {@return the pitch of the voice of this mob}: as in Vanilla, around {@code 1.0}, or
     * {@code 1.5} for a baby
     */
    protected float voicePitch() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final float first = random.nextFloat();
        final float second = random.nextFloat();
        return (first - second) * 0.2f + (isBaby() ? 1.5f : 1.0f);
    }

    public float attackDamage() {
        return 0f;
    }

    protected float soundVolume() {
        return 1.0f;
    }

    public void playHurtSound() {
        final Sound.Type sound = hurtSound();
        if (sound != null) {
            playSound(sound, soundVolume(), voicePitch());
        }
    }

    public final void notifyHurt(final DamageSource source, final float amount) {
        onHurt(source, amount);
        dispatch(behaviour -> behaviour.onHurt(source, amount), "onHurt");
    }

    public void onHurt(final DamageSource source, final float amount) {
    }

    /**
     * What this kind of mob does when a player uses an item on it, unless a behaviour handled the
     * click first.
     *
     * @return {@code true} if the click did something
     */
    protected boolean interact(final ServerPlayer player, final EquipmentSlotGroup hand) {
        return false;
    }

    /**
     * {@return the slot of the inventory of a player the given hand holds}
     */
    protected static int handSlot(final ServerPlayer player, final EquipmentSlotGroup hand) {
        return hand == EquipmentSlotGroup.OFF_HAND ? PlayerInventory.OFF_HAND : player.selectedSlot();
    }

    /**
     * Turns one item of a hand into another, fills a bucket or a bowl: the last item of
     * the stack becomes the result, otherwise the result goes to the inventory, or to the ground if
     * it is full. In creative mode the item is kept and the result is given.
     *
     * @param limitCreativeStackSize whether a creative player only gets the result if they have none
     */
    protected final void fillItem(final ServerPlayer player, final int slot, final ItemStack result,
                                  final boolean limitCreativeStackSize) {
        final PlayerInventory inventory = player.inventory();
        final boolean creative = player.gameMode() == GameMode.CREATIVE;
        if (creative && limitCreativeStackSize) {
            if (!holds(inventory, result)) {
                PlayerInventories.add(inventory, player.selectedSlot(), result);
            }
            return;
        }
        if (!creative) {
            final ItemStack held = inventory.get(slot);
            if (held.count() <= 1) {
                inventory.set(slot, result);
                return;
            }
            inventory.set(slot, held.withCount(held.count() - 1));
        }
        final ItemStack left = PlayerInventories.add(inventory, player.selectedSlot(), result);
        if (!left.isEmpty()) {
            server().entities().dropFromPlayer(player, left);
        }
    }

    private static boolean holds(final PlayerInventory inventory, final ItemStack stack) {
        for (int slot = 0, size = inventory.size(); slot < size; slot++) {
            if (stack.isSimilar(inventory.get(slot))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Wears the tool of a hand by one use; it is gone once it breaks. A creative player wears nothing.
     */
    protected static void wearItem(final ServerPlayer player, final int slot) {
        if (player.gameMode() == GameMode.CREATIVE) {
            return;
        }
        // TODO: Unbreaking, and the sound and particles of a tool that breaks
        final ItemStack tool = player.inventory().get(slot);
        final int durability = ItemDefaults.maxDamage(tool.id(), tool);
        if (durability <= 0) {
            return;
        }
        final int damage = tool.getOrDefault(DataComponentTypes.DAMAGE, 0) + 1;
        player.inventory().set(slot, damage >= durability ? ItemStack.EMPTY : tool.with(DataComponentTypes.DAMAGE, damage));
    }

    /**
     * Makes this mob count as hit by a player for as long as another mob does, as when vanilla
     * turns a mob into another.
     */
    protected final void copyHurtByPlayerFrom(final AbstractMob other) {
        this.hurtByPlayerTicks = other.hurtByPlayerTicks;
    }

    public final boolean onInteract(final Player player, final EquipmentSlotGroup hand) {
        final boolean handledByBehaviour = dispatchInteract(player, hand);
        if (handledByBehaviour || isRemoved() || isDead()
                || !(player instanceof final ServerPlayer serverPlayer)
                || serverPlayer.gameMode() == GameMode.SPECTATOR) {
            return handledByBehaviour;
        }
        return interact(serverPlayer, hand);
    }

    private boolean dispatchInteract(final Player player, final EquipmentSlotGroup hand) {
        boolean handled = false;
        for (final MobBehaviour behaviour : behaviours) {
            try {
                handled |= behaviour.onInteract(player, hand);
            } catch (final Throwable throwable) {
                LOGGER.error("Error in the onInteract of {} on {}", behaviour.getClass().getName(), this, throwable);
            }
        }
        return handled;
    }

    public final List<MobBehaviour> behaviours() {
        return List.copyOf(behaviours);
    }

    public final <T extends MobBehaviour> Optional<T> behaviour(final Class<T> type) {
        for (final MobBehaviour behaviour : behaviours) {
            if (type.isInstance(behaviour)) {
                return Optional.of(type.cast(behaviour));
            }
        }
        return Optional.empty();
    }

    public final void addBehaviour(final MobBehaviour behaviour) {
        behaviours.add(behaviour);
        try {
            behaviour.onSpawn();
        } catch (final Throwable throwable) {
            LOGGER.error("Error in the onSpawn of {} on {}", behaviour.getClass().getName(), this, throwable);
        }
    }

    public final boolean hasBehaviours() {
        return !behaviours.isEmpty();
    }

    protected final void tickBehaviours(final long currentTick) {
        if (behaviours.isEmpty()) {
            return;
        }
        dispatch(behaviour -> behaviour.onTick(currentTick), "onTick");
    }

    public final void playPositionalSound(final Sound.Type type, final float volume, final float pitch) {
        final Location loc = location();
        sendToTrackers(new ClientboundSoundPacket(
                Sound.sound(type, soundSource(), volume, pitch), loc.x(), loc.y(), loc.z()));
    }

    public final void playSound(final Sound.Type type, final float volume, final float pitch) {
        sendToTrackers(new ClientboundSoundEntityPacket(
                Sound.sound(type, soundSource(), volume, pitch), entityId()));
    }

    private void dispatch(final Consumer<MobBehaviour> action, final String hook) {
        for (final MobBehaviour behaviour : behaviours) {
            try {
                action.accept(behaviour);
            } catch (final Throwable throwable) {
                LOGGER.error("Error in the {} of {} on {}", hook, behaviour.getClass().getName(), this, throwable);
            }
        }
    }
}
