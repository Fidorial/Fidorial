package fr.euphyllia.fidorial.server.entity.mob;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.GameMode;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.keys.EntityTypeTagKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;

import java.util.UUID;

/**
 * A mob that is born a baby and grows up, with vanilla's age: negative for a baby, counting up to
 * {@code 0}; positive for an adult that cannot breed yet, counting down to {@code 0}.
 */
public abstract class AbstractAgeableMob extends AbstractPathfinderMob {

    /**
     * The age of a newborn: it grows up in 20 minutes.
     */
    public static final int BABY_START_AGE = -24_000;

    private static final int MD_BABY = 16;
    private static final int MD_AGE_LOCKED = 17;

    private static final int AGE_LOCK_COOLDOWN_TICKS = 40;
    private static final float FEED_GROWTH_FRACTION = 0.1f;

    private int age;
    private int forcedAge;
    private boolean ageLocked;
    private int ageLockCooldown;

    protected AbstractAgeableMob(final int entityId, final UUID uuid, final EntityType type,
                                 final Location location, final float maxHealth, final boolean baby) {
        super(entityId, uuid, type, location, maxHealth);
        this.age = baby ? BABY_START_AGE : 0;
    }

    @Override
    public void tick(final long currentTick) {
        super.tick(currentTick);
        if (isRemoved() || isDead()) {
            return;
        }

        if (ageLockCooldown > 0) {
            ageLockCooldown--;
        }

        if (canAgeUp()) {
            setAge(age + 1);
        } else if (age > 0) {
            setAge(age - 1);
        }
    }

    @Override
    public final boolean isBaby() {
        return age < 0;
    }

    public final void setBaby(final boolean baby) {
        setAge(baby ? BABY_START_AGE : 0);
    }

    /**
     * {@return the age of this mob in ticks}: negative until it grows up, positive while it cannot
     * breed again, {@code 0} otherwise
     */
    public final int age() {
        return age;
    }

    public final void setAge(final int newAge) {
        final boolean wasBaby = age < 0;
        this.age = newAge;
        if (wasBaby != (newAge < 0)) {
            sendAgeMetadata();
            onAgeBoundaryReached();
        }
    }

    /**
     * Brings a baby closer to growing up, as vanilla does when it is fed.
     *
     * @param seconds how much older the baby gets
     * @param forced  whether the growth was forced, by feeding it
     */
    public final void ageUp(final int seconds, final boolean forced) {
        final int before = age;
        int after = before + seconds * 20;
        if (after > 0) {
            after = 0;
        }
        setAge(after);
        if (forced) {
            forcedAge += after - before;
        }
        if (age == 0) {
            setAge(forcedAge);
        }
    }

    /**
     * {@return by how many seconds a meal ages a baby}: a tenth of the time it has left, as in Vanilla
     *
     * @param ticksUntilAdult how long the baby still has to grow
     */
    public static int speedUpSecondsWhenFeeding(final int ticksUntilAdult) {
        final int secondsUntilAdult = ticksUntilAdult / 20;
        return (int) (secondsUntilAdult * FEED_GROWTH_FRACTION);
    }

    /**
     * Makes this mob as old as another, as when vanilla turns a mob into another.
     */
    protected final void copyAgeFrom(final AbstractAgeableMob other) {
        setAge(other.age);
        this.forcedAge = other.forcedAge;
    }

    public final int forcedAge() {
        return forcedAge;
    }

    public final boolean canAgeUp() {
        return isBaby() && !ageLocked;
    }

    /**
     * {@return {@code true} if a golden dandelion stopped this baby from growing}
     */
    public final boolean isAgeLocked() {
        return ageLocked;
    }

    public final void setAgeLocked(final boolean ageLocked) {
        if (this.ageLocked != ageLocked) {
            this.ageLocked = ageLocked;
            sendAgeMetadata();
        }
    }

    /**
     * Called when this mob grows up, or becomes a baby again.
     */
    protected void onAgeBoundaryReached() {
    }

    @Override
    protected boolean interact(final ServerPlayer player, final EquipmentSlotGroup hand) {
        final int slot = handSlot(player, hand);
        final ItemStack held = player.inventory().get(slot);
        if (!held.isEmpty() && held.id().equals(ItemKeys.GOLDEN_DANDELION) && canUseGoldenDandelion()) {
            setAgeLocked(!ageLocked);
            setAge(BABY_START_AGE);
            ageLockCooldown = AGE_LOCK_COOLDOWN_TICKS;
            consumeItem(player, slot);
            final Location at = location();
            final ClientboundSoundPacket sound = new ClientboundSoundPacket(
                    Sound.sound(ageLocked ? SoundEvents.GOLDEN_DANDELION_USE : SoundEvents.GOLDEN_DANDELION_UNUSE,
                            Sound.Source.PLAYER, 1.0f, 1.0f),
                    Math.floor(at.x()) + 0.5, Math.floor(at.y()) + 0.5, Math.floor(at.z()) + 0.5);
            sendToTrackers(sound);
            return true;
        }
        return super.interact(player, hand);
    }

    private boolean canUseGoldenDandelion() {
        return isBaby() && ageLockCooldown == 0
                && !server().registries().registry(RegistryKey.ENTITY_TYPE)
                .isTagged(type().key(), EntityTypeTagKeys.CANNOT_BE_AGE_LOCKED);
    }

    /**
     * Takes one item from a hand of a player, unless the player is in creative mode.
     */
    protected static void consumeItem(final ServerPlayer player, final int slot) {
        if (player.gameMode() == GameMode.CREATIVE) {
            return;
        }
        final ItemStack held = player.inventory().get(slot);
        player.inventory().set(slot, held.count() <= 1 ? ItemStack.EMPTY : held.withCount(held.count() - 1));
    }

    protected abstract double adultHeight();

    protected abstract double adultWidth();

    protected double babyHeight() {
        return adultHeight() * 0.5;
    }

    protected double babyWidth() {
        return adultWidth() * 0.5;
    }

    @Override
    public final double height() {
        return isBaby() ? babyHeight() : adultHeight();
    }

    @Override
    public final double width() {
        return isBaby() ? babyWidth() : adultWidth();
    }

    @Override
    protected final double halfWidth() {
        return width() * 0.5;
    }

    @Override
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
        super.saveAdditional(tag);
        tag.putInt("Age", age);
        tag.putInt("ForcedAge", forcedAge);
        tag.putBoolean("AgeLocked", ageLocked);
    }

    @Override
    public void loadAdditional(final CompoundBinaryTag tag) {
        super.loadAdditional(tag);
        setAge(tag.getInt("Age", 0));
        this.forcedAge = tag.getInt("ForcedAge", 0);
        setAgeLocked(tag.getBoolean("AgeLocked", false));
    }


    private ClientboundSetEntityMetadataPacket ageMetadata() {
        return ClientboundSetEntityMetadataPacket.of(entityId(),
                ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_BABY, isBaby()),
                ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_AGE_LOCKED, ageLocked));
    }

    private void sendAgeMetadata() {
        sendToTrackers(ageMetadata());
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        super.sendSpawnPackets(connection);
        connection.send(ageMetadata());
    }
}
