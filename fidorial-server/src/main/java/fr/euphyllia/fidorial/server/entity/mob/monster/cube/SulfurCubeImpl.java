package fr.euphyllia.fidorial.server.entity.mob.monster.cube;

import fr.euphyllia.fidorial.server.codecs.container.ContainerCodecs;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.item.ServerItemEntity;
import fr.euphyllia.fidorial.server.entity.mob.MobAttribute;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMotionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEquipmentPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundTakeItemEntityPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.entity.mob.monster.SulfurCube;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.data.DamageType;
import fr.fidorial.registry.keys.DamageTypeTagKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.ItemTagKeys;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;

/**
 * The sulfur cube: a cube that hurts no one and chases nothing. It is born a baby of size 1 and
 * grows up into an adult of size 2, sooner when fed slime balls; health is four times its size.
 * A baby follows the players holding slime balls, an adult those holding a block it could swallow.
 * Once dead, an adult splits into two babies.
 *
 * <p>An adult swallows such a block when a player gives it one, or when it finds one lying around.
 * It then stops moving by itself and becomes a ball: the block it holds, through the entries of the
 * {@code sulfur_cube_archetype} registry, sets how it bounces, slides, floats, how far a hit sends it
 * and the damage it deals by touch. Players push it by walking into it; blows and shots only send it
 * flying. Shears make it spit the block out, and it waits five seconds before it picks up another.
 * The block always falls when it dies.</p>
 *
 * <p>It stays in a peaceful world, and sounds like a neutral mob.</p>
 */
public final class SulfurCubeImpl extends AbstractCubeMob implements SulfurCube {

    private static final double BASE_EYE_HEIGHT = 0.175;
    private static final float HEALTH_PER_SIZE = 4.0f;
    private static final int SPLIT_COUNT = 2;
    private static final int TEMPT_PRIORITY = 2;
    private static final double TEMPT_RANGE = 8.0;
    private static final double TEMPT_STOP_DISTANCE = 1.0;
    private static final int SEARCH_PRIORITY = 3;
    private static final double SEARCH_RANGE = 8.0;
    private static final int MAX_EXTRA_EXPERIENCE = 1;
    /**
     * How long a sulfur cube that spat its block out waits before it picks up another, in ticks.
     */
    private static final int PICKUP_COOLDOWN = 100;
    private static final double PICKUP_REACH = 1.0;
    private static final double FLUID_JUMP_THRESHOLD_SHARE = 0.2;
    private static final double ITEM_HEIGHT = 0.25;

    /**
     * How close across a player has to come to push a cube holding a block.
     */
    private static final double PUSH_DISTANCE = 1.3;
    private static final double MAX_PUSH_SPEED = 0.5;
    private static final double PUSH_SPEED_SCALE = 0.3;
    private static final double VERTICAL_PUSH = 0.3;
    private static final float FLOAT_SWAY = 0.2f;
    private static final float FLOAT_SWAY_SPEED = 0.4f;
    private static final double FLOAT_LIFT = 0.04;

    /**
     * How hard a hit pushes any mob, the share of a hit that comes from it alone.
     */
    private static final double BASE_KNOCKBACK = 0.4;
    private static final float HORIZONTAL_HIT_ANGLE_SCALE = 1.6f;
    private static final float VERTICAL_HIT_ANGLE_SCALE = 0.5f;
    private static final float VERTICAL_POSITION_ANGLE_SCALE = 0.8f;
    private static final float EXTRA_KNOCKBACK_SHARE = 0.25f;
    private static final float HORIZONTAL_KNOCKBACK_SCALE = 0.4f;
    private static final float VERTICAL_KNOCKBACK_SCALE = 1.2f;
    private static final float MAX_KNOCKBACK_POWER = 128.0f;
    private static final double MIN_KNOCKBACK_RESISTANCE = -2.0;

    private static final String TAG_EQUIPMENT = "equipment";
    private static final String TAG_DROP_CHANCES = "drop_chances";
    private static final String TAG_PICKUP_TIMER = "pickup_timer";
    private static final float GUARANTEED_DROP = 2.0f;

    private final MobAttribute knockbackResistanceAttribute = new MobAttribute(Key.key("knockback_resistance"), 0.0);
    private final MobAttribute explosionKnockbackResistanceAttribute =
            new MobAttribute(Key.key("explosion_knockback_resistance"), 0.0);
    private final MobAttribute bouncinessAttribute = new MobAttribute(Key.key("bounciness"), 0.0);
    private final MobAttribute frictionModifierAttribute = new MobAttribute(Key.key("friction_modifier"), 1.0);
    private final MobAttribute airDragModifierAttribute = new MobAttribute(Key.key("air_drag_modifier"), 1.0);
    private final Map<Key, MobAttribute> attributes = Map.of(
            knockbackResistanceAttribute.id(), knockbackResistanceAttribute,
            explosionKnockbackResistanceAttribute.id(), explosionKnockbackResistanceAttribute,
            bouncinessAttribute.id(), bouncinessAttribute,
            frictionModifierAttribute.id(), frictionModifierAttribute,
            airDragModifierAttribute.id(), airDragModifierAttribute);

    /**
     * The goals of a cube that holds nothing; one that holds a block has none.
     */
    private final List<Goal> freeGoals;
    private ItemStack swallowed = ItemStack.EMPTY;
    private List<SulfurCubeArchetypes.Archetype> archetypes = List.of();
    private boolean floatsInLiquids;
    private SulfurCubeArchetypes.Knockback knockback = SulfurCubeArchetypes.DEFAULT_KNOCKBACK;
    private SulfurCubeArchetypes.Sounds sounds = SulfurCubeArchetypes.DEFAULT_SOUNDS;
    private List<SulfurCubeArchetypes.ContactDamage> contactDamages = List.of();
    private int pickupTimer;
    private int pushSoundCooldown;
    private int tickCount;

    /**
     * Creates an adult sulfur cube as one that spawns.
     */
    public SulfurCubeImpl(final int entityId, final Location location) {
        this(entityId, location, true);
    }

    private SulfurCubeImpl(final int entityId, final Location location, final boolean spawned) {
        super(entityId, EntityTypes.SULFUR_CUBE, location, spawned);
        goals.add(new CubeTemptGoal(TEMPT_PRIORITY, this::isTempting, TEMPT_RANGE, TEMPT_STOP_DISTANCE));
        goals.add(new SearchForItemsGoal());
        freeGoals = List.copyOf(goals.all());
    }

    @Override
    protected AbstractCubeMob newSplitCube(final int entityId, final Location location) {
        return new SulfurCubeImpl(entityId, location, false);
    }

    /**
     * {@return {@code true} if an item tempts this cube}: slime balls for a baby, the blocks it
     * could swallow for an adult
     */
    private boolean isTempting(final ItemStack stack) {
        return isBaby() ? isFood(stack) : isSwallowable(stack);
    }

    private boolean isFood(final ItemStack stack) {
        return server().registries().registry(RegistryKey.ITEM).isTagged(stack.id(), ItemTagKeys.SULFUR_CUBE_FOOD);
    }

    private boolean isSwallowable(final ItemStack stack) {
        return !stack.isEmpty()
                && server().registries().registry(RegistryKey.ITEM).isTagged(stack.id(), ItemTagKeys.SULFUR_CUBE_SWALLOWABLE);
    }

    private boolean isDamageTagged(final DamageSource source, final TagKey<DamageType> tag) {
        return server().registries().registry(RegistryKey.DAMAGE_TYPE).isTagged(source.type(), tag);
    }

    /**
     * {@return {@code true} if this cube holds a block}
     */
    public boolean hasSwallowedItem() {
        return !swallowed.isEmpty();
    }

    @Override
    public ItemStack swallowedItem() {
        return swallowed;
    }

    @Override
    public void setSwallowedItem(final ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        final ItemStack kept = stack.isEmpty() ? ItemStack.EMPTY : stack.withCount(1);
        final ItemStack previous = swallowed;
        swallowed = kept;
        if (previous.equals(kept)) {
            return;
        }
        if (previous.isEmpty() != kept.isEmpty()) {
            // a cube holding a block is a ball: it does nothing by itself
            goals.stopAll();
            goals.clear();
            if (kept.isEmpty()) {
                freeGoals.forEach(goals::add);
            } else {
                setWantedMovement(0.0);
                setForwardSpeed(0.0);
            }
        }
        applyArchetypes(SulfurCubeArchetypes.matching(kept));
        sendToTrackers(ClientboundSetEquipmentPacket.of(entityId(), EquipmentSlot.BODY, kept));
    }

    /**
     * Takes off what the entries of the previous block gave this cube, then gives it what the
     * entries of its block give; the last entry wins for the knockback and the sounds.
     */
    private void applyArchetypes(final List<SulfurCubeArchetypes.Archetype> matching) {
        for (final SulfurCubeArchetypes.Archetype archetype : archetypes) {
            for (final SulfurCubeArchetypes.Modifier modifier : archetype.modifiers()) {
                final MobAttribute attribute = attributes.get(modifier.attribute());
                if (attribute != null) {
                    attribute.remove(modifier.id());
                }
            }
        }
        archetypes = matching;
        floatsInLiquids = false;
        knockback = SulfurCubeArchetypes.DEFAULT_KNOCKBACK;
        sounds = SulfurCubeArchetypes.DEFAULT_SOUNDS;
        final List<SulfurCubeArchetypes.ContactDamage> damages = new ArrayList<>();
        for (final SulfurCubeArchetypes.Archetype archetype : matching) {
            floatsInLiquids |= archetype.buoyant();
            if (archetype.contactDamage() != null) {
                damages.add(archetype.contactDamage());
            }
            knockback = archetype.knockback();
            sounds = archetype.sounds();
            for (final SulfurCubeArchetypes.Modifier modifier : archetype.modifiers()) {
                final MobAttribute attribute = attributes.get(modifier.attribute());
                if (attribute != null) {
                    attribute.put(modifier.id(), modifier.amount(), modifier.operation());
                }
            }
        }
        contactDamages = List.copyOf(damages);
    }

    /**
     Swallows one of an item: an adult that already holds another spits it out first; a baby, or
     * an adult that already holds the same item, does nothing.
     *
     * @return {@code true} if this cube swallowed it
     */
    private boolean swallow(final ItemStack stack) {
        if (isBaby() || (hasSwallowedItem() && swallowed.id().equals(stack.id()))) {
            return false;
        }
        if (hasSwallowedItem()) {
            dropFromTop(swallowed);
        }
        setSwallowedItem(stack);
        playSound(SoundEvents.SULFUR_CUBE_ABSORB, 1.0f, 1.0f);
        return true;
    }

    @Override
    public boolean readyForShearing() {
        return hasSwallowedItem();
    }

    /**
     * Makes this cube spit its block out, which falls from its top; it waits five seconds before it
     * picks up another.
     */
    @Override
    public void shear() {
        final ItemStack spat = swallowed;
        if (spat.isEmpty()) {
            return;
        }
        setSwallowedItem(ItemStack.EMPTY);
        dropFromTop(spat);
        playSound(SoundEvents.SULFUR_CUBE_EJECT, 1.0f, 1.0f);
        pickupTimer = PICKUP_COOLDOWN;
    }

    /**
     * Drops a stack from the top of this cube, thrown up a little in a random direction.
     */
    private void dropFromTop(final ItemStack stack) {
        if (stack.isEmpty() || !(world() instanceof final ServerWorld serverWorld)) {
            return;
        }
        final Location at = location();
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        server().entities().dropItem(serverWorld, at.x(), at.y() + height(), at.z(), stack,
                random.nextDouble() * 0.2 - 0.1, 0.2, random.nextDouble() * 0.2 - 0.1,
                ItemEntity.DEFAULT_PICKUP_DELAY, null);
    }

    /**
     * A baby fed a slime ball grows up a tenth of the time it has left. An adult gives up its block
     * to shears, and swallows the block a player gives it.
     */
    @Override
    protected boolean interact(final ServerPlayer player, final EquipmentSlotGroup hand) {
        final int slot = handSlot(player, hand);
        final ItemStack held = player.inventory().get(slot);
        if (isBaby()) {
            if (!held.isEmpty() && isFood(held) && canAgeUp()) {
                consumeItem(player, slot);
                ageUp(speedUpSecondsWhenFeeding(-age()), true);
                playSound(SoundEvents.SMALL_SULFUR_CUBE_EAT, soundVolume(), voicePitch());
                return true;
            }
            return super.interact(player, hand);
        }
        if (held.id().equals(ItemKeys.SHEARS) && readyForShearing()) {
            shear();
            server().debug().gameEvents().emit(world(), GameEventKeys.SHEAR, location());
            wearItem(player, slot);
            return true;
        }
        if (isSwallowable(held)) {
            if (!swallow(held)) {
                return false;
            }
            consumeItem(player, slot);
            server().debug().gameEvents().emit(world(), GameEventKeys.ENTITY_INTERACT, location());
            return true;
        }
        return super.interact(player, hand);
    }

    /**
     * Sends a cube holding a block away from a player walking into it, the faster they walk the
     * further; it pushes it up a little too if it lies on the ground.
     */
    private void pushedBy(final ServerPlayer player) {
        final Location at = location();
        final Location pusher = player.location();
        final double dx = at.x() - pusher.x();
        final double dz = at.z() - pusher.z();
        final double across = Math.sqrt(dx * dx + dz * dz);
        if (across >= PUSH_DISTANCE || pusher.y() > at.y() + height() || pusher.y() + boxHeightOf(player) <= at.y()) {
            return;
        }
        final double kept = Math.max(0.0, 1.0 - knockbackResistance());
        final double speed = Math.clamp(player.knownSpeed() * 2.0 * PUSH_SPEED_SCALE, 0.0, MAX_PUSH_SPEED);
        final double pushX = across > 1.0E-7 ? dx / across * kept * speed : 0.0;
        final double pushZ = across > 1.0E-7 ? dz / across * kept * speed : 0.0;
        final double pushY = onGround() ? kept * VERTICAL_PUSH * speed : 0.0;
        final Sound.Type pushSound = sounds.push();
        final double threshold = sounds.pushImpulseThreshold();
        if (pushSound != null && pushSoundCooldown <= 0
                && pushX * pushX + pushY * pushY + pushZ * pushZ > threshold * threshold) {
            pushSoundCooldown = (int) (sounds.pushCooldown() * 20.0f);
            playSound(pushSound, 1.0f, 1.0f);
        }
        addVelocity(pushX, pushY, pushZ);
        dealContactDamage(player);
    }

    private void addVelocity(final double x, final double y, final double z) {
        final double newX = velocityX() + x;
        final double newY = velocityY() + y;
        final double newZ = velocityZ() + z;
        setVelocity(newX, newY, newZ);
        sendToTrackers(new ClientboundSetEntityMotionPacket(entityId(), new PositionData.VelocityVec3D(newX, newY, newZ)));
    }

    /**
     * Deals what touching this cube costs, as its block says: a hot one burns.
     */
    private void dealContactDamage(final AbstractLivingEntity target) {
        for (final SulfurCubeArchetypes.ContactDamage damage : contactDamages) {
            final Entity credited = damage.attributeToSource() || target instanceof ServerPlayer ? this : null;
            server().entities().combat().damage(target, DamageSource.of(damage.type(), credited), damage.sample());
        }
    }

    /**
     * A cube holding a block takes no harm from blows, shots and the like, the types of the
     * {@code sulfur_cube_with_block_immune_to} tag: they only send it flying.
     */
    @Override
    public boolean takeHit(final DamageSource source, final float amount, final double strength, final BooleanSupplier hurt) {
        if (hasSwallowedItem() && isDamageTagged(source, DamageTypeTagKeys.SULFUR_CUBE_WITH_BLOCK_IMMUNE_TO)) {
            if (!isDamageTagged(source, DamageTypeTagKeys.NO_KNOCKBACK)) {
                knockBack(source, amount, strength);
            }
            return true;
        }
        return hurt.getAsBoolean();
    }

    /**
     * Sends a cube holding a block flying its own way: as far as its block says, further the harder
     * the hit, more along where the attacker aims and less straight up the higher the attacker
     * stands. A sprinting attacker, or one whose weapon knocks back, sends it once more the way it
     * faces.
     */
    @Override
    public boolean knockBack(final DamageSource source, final float damage, final double strength) {
        final Entity attacker = source.causingEntity();
        if (attacker == null || !hasSwallowedItem()) {
            return false;
        }
        final Location at = location();
        final Location from = source.position() != null ? source.position()
                : source.directEntity() != null ? source.directEntity().location() : attacker.location();
        sendFlying(attacker, from.x() - at.x(), from.z() - at.z(), damage, BASE_KNOCKBACK, false);
        if (strength > BASE_KNOCKBACK) {
            final double yaw = Math.toRadians(attacker.location().yaw());
            sendFlying(attacker, Math.sin(yaw), -Math.cos(yaw), damage, strength - BASE_KNOCKBACK, true);
        }
        return true;
    }

    /**
     * Sends this cube away from a direction.
     *
     * @param attacker   who hit it
     * @param towardsX   where the hit came from, across, from this cube
     * @param towardsZ   where the hit came from, across, from this cube
     * @param damage     the damage of the hit
     * @param power      how hard the hit pushes
     * @param fromEffect {@code true} for the extra push of a sprint or a weapon
     */
    private void sendFlying(final Entity attacker, final double towardsX, final double towardsZ, final float damage,
                            final double power, final boolean fromEffect) {
        final Location feet = attacker.location();
        final double eyeX = feet.x();
        final double eyeY = feet.y() + eyeHeightOf(attacker);
        final double eyeZ = feet.z();
        final double pitch = Math.toRadians(feet.pitch());
        final double yaw = Math.toRadians(-feet.yaw());
        final double aimX = Math.sin(yaw) * Math.cos(pitch);
        final double aimY = -Math.sin(pitch);
        final double aimZ = Math.cos(yaw) * Math.cos(pitch);
        final Location at = location();
        final double centerX = at.x();
        final double centerY = at.y() + height() / 2.0;
        final double centerZ = at.z();

        // the hit turns the way it goes towards where the attacker aims
        final Direction toCenter = normalized(centerX - eyeX, centerY - eyeY, centerZ - eyeZ);
        final double turn = Math.atan2(aimX * toCenter.z() - aimZ * toCenter.x(), aimX * toCenter.x() + aimZ * toCenter.z())
                * HORIZONTAL_HIT_ANGLE_SCALE;
        final double cos = Math.cos(turn);
        final double sin = Math.sin(turn);
        final double directionX = towardsX * cos - towardsZ * sin;
        final double directionZ = towardsZ * cos + towardsX * sin;

        // a hit aimed low sends it up, a hit aimed high flatter
        final float horizontal = knockback.horizontalPower();
        final float vertical = knockback.verticalPower();
        final double halfHeight = height() / 2.0;
        final double topY = normalized(centerX - eyeX, centerY + halfHeight - eyeY, centerZ - eyeZ).y();
        final double bottomY = normalized(centerX - eyeX, centerY - halfHeight - eyeY, centerZ - eyeZ).y();
        final double aimed = clampedMap(aimY, topY, bottomY, -1.0, 1.0);
        double transferred = Math.abs(aimed * VERTICAL_HIT_ANGLE_SCALE);
        if (aimed < 0.0) {
            transferred = -transferred;
        }
        double powerX = horizontal * (1.0 - transferred);
        double powerY = vertical * (1.0 + transferred);

        // an attacker standing above sends it flatter, one standing below more upwards
        final double below = at.y() - feet.y();
        final double acrossToIt = Math.sqrt((at.x() - feet.x()) * (at.x() - feet.x()) + (at.z() - feet.z()) * (at.z() - feet.z()));
        final double angle = -Math.atan2(-below, acrossToIt) * VERTICAL_POSITION_ANGLE_SCALE;
        final double rotatedX = powerX * Math.cos(angle) - powerY * Math.sin(angle);
        final double rotatedY = powerY * Math.cos(angle) + powerX * Math.sin(angle);
        final double ratioX = horizontal > 0.0f ? Math.abs(rotatedX) / horizontal : 0.0;
        final double ratioY = vertical > 0.0f ? Math.abs(rotatedY) / vertical : 0.0;
        final double ratio = Math.max(ratioX, ratioY);
        powerX = ratio > 1.0 ? rotatedX / ratio : rotatedX;
        powerY = ratio > 1.0 ? rotatedY / ratio : rotatedY;

        final double multiplier = Math.sqrt(damage) * (fromEffect ? power * EXTRA_KNOCKBACK_SHARE : 1.0);
        final double kept = 1.0 - knockbackResistance();
        powerX *= multiplier * kept * HORIZONTAL_KNOCKBACK_SCALE;
        powerY *= multiplier * kept;
        powerX = Math.clamp(powerX, -MAX_KNOCKBACK_POWER, MAX_KNOCKBACK_POWER);
        powerY = Math.clamp(powerY, -MAX_KNOCKBACK_POWER, MAX_KNOCKBACK_POWER);
        final Direction away = normalized(directionX, 0.0, directionZ);
        addVelocity(-away.x() * powerX, powerY * VERTICAL_KNOCKBACK_SCALE, -away.z() * powerX);
        if (sounds.hit() != null) {
            playSound(sounds.hit(), 1.0f, 1.0f);
        }
    }

    private static Direction normalized(final double x, final double y, final double z) {
        final double length = Math.sqrt(x * x + y * y + z * z);
        return length < 1.0E-5 ? new Direction(0.0, 0.0, 0.0) : new Direction(x / length, y / length, z / length);
    }

    /**
     * A direction of length 1, or none.
     */
    private record Direction(double x, double y, double z) {
    }

    private static double clampedMap(final double value, final double fromMin, final double fromMax,
                                     final double toMin, final double toMax) {
        final double t = (value - fromMin) / (fromMax - fromMin);
        if (t < 0.0) {
            return toMin;
        }
        return t > 1.0 ? toMax : toMin + t * (toMax - toMin);
    }

    @Override
    public double knockbackResistance() {
        return Math.clamp(knockbackResistanceAttribute.value(), MIN_KNOCKBACK_RESISTANCE, 1.0);
    }

    /**
     * {@return how much less an explosion sends this cube flying}
     */
    public double explosionKnockbackResistance() {
        return Math.clamp(explosionKnockbackResistanceAttribute.value(), 0.0, 1.0);
    }

    @Override
    public void tick(final long currentTick) {
        tickCount++;
        if (pickupTimer > 0) {
            pickupTimer--;
        }
        if (pushSoundCooldown > 0) {
            pushSoundCooldown--;
        }
        super.tick(currentTick);
        if (isRemoved() || isDead()) {
            return;
        }
        if (hasSwallowedItem()) {
            for (final ServerPlayer player : server().players().online()) {
                if (isTouchedBy(player)) {
                    pushedBy(player);
                }
            }
            if (!contactDamages.isEmpty()) {
                burnWhatTouchesIt();
            }
        } else {
            pickUpItems();
        }
    }

    /**
     * Deals the damage of its block to the living entities whose box meets the box of this cube.
     */
    private void burnWhatTouchesIt() {
        final Location at = location();
        final double half = halfWidth();
        serverWorld().entityManager().forEachNear(at.chunk(), width() + 2.0, entity -> {
            if (entity == this || !(entity instanceof final AbstractLivingEntity living) || living.isDead()
                    || (living instanceof final ServerPlayer player && player.gameMode() == GameMode.SPECTATOR)) {
                return;
            }
            final Location other = living.location();
            final double reach = half + halfWidthOf(living);
            if (Math.abs(other.x() - at.x()) < reach && Math.abs(other.z() - at.z()) < reach
                    && other.y() < at.y() + height() && at.y() < other.y() + boxHeightOf(living)) {
                dealContactDamage(living);
            }
        });
    }

    /**
     * Picks up a block lying within a block across, if the game rule {@code mob_griefing} lets mobs
     * pick things up.
     */
    private void pickUpItems() {
        if (isBaby() || pickupTimer > 0 || !serverWorld().gameRuleValues().getBoolean(GameRuleKeys.MOB_GRIEFING)) {
            return;
        }
        final Location at = location();
        final double reach = halfWidth() + PICKUP_REACH;
        final List<ServerItemEntity> near = new ArrayList<>();
        serverWorld().entityManager().forEachNear(at.chunk(), reach + 1.0, entity -> {
            if (entity instanceof final ServerItemEntity drop && !drop.isRemoved() && drop.pickupDelay() == 0
                    && isSwallowable(drop.item())) {
                final Location other = drop.location();
                if (Math.abs(other.x() - at.x()) < reach && Math.abs(other.z() - at.z()) < reach
                        && other.y() < at.y() + height() && at.y() < other.y() + ITEM_HEIGHT) {
                    near.add(drop);
                }
            }
        });
        for (final ServerItemEntity drop : near) {
            if (hasSwallowedItem()) {
                return;
            }
            final ItemStack stack = drop.item();
            setSwallowedItem(stack);
            playSound(SoundEvents.SULFUR_CUBE_ABSORB, 1.0f, 1.0f);
            sendToTrackers(new ClientboundTakeItemEntityPacket(drop.entityId(), entityId(), 1));
            drop.setItem(stack.withCount(stack.count() - 1));
        }
    }

    /**
     * A cube holding a block does not hop: it only snaps to face a side.
     */
    @Override
    protected void tickMoveControl() {
        if (!hasSwallowedItem()) {
            super.tickMoveControl();
            return;
        }
        final float yaw = yaw();
        final float closest = Math.round(yaw / 90.0f) * 90.0f;
        if (Math.abs(yaw - closest) > 1.0E-4f) {
            setRotation(closest, 0f);
        }
    }

    @Override
    protected double bounciness() {
        return Math.clamp(bouncinessAttribute.value(), 0.0, 1.0);
    }

    @Override
    protected double frictionModifier() {
        return Math.clamp(frictionModifierAttribute.value(), 0.0, 2048.0);
    }

    @Override
    protected double airDragModifier() {
        return Math.clamp(airDragModifierAttribute.value(), 0.0, 2048.0);
    }

    @Override
    protected boolean omnidirectionalAirMover() {
        return hasSwallowedItem();
    }

    /**
     * A cube holding a block that floats bobs on water and lava.
     */
    @Override
    protected double liftInFluid(final double depth) {
        if (!hasSwallowedItem() || !floatsInLiquids) {
            return 0.0;
        }
        final double sway = FLOAT_SWAY * Math.sin(tickCount * FLOAT_SWAY_SPEED);
        final double immersion = depth - swimThreshold() + sway;
        return immersion > 0.0 ? Math.min(1.0, immersion) * FLOAT_LIFT : 0.0;
    }

    @Override
    public double swimThreshold() {
        return height() * FLUID_JUMP_THRESHOLD_SHARE;
    }

    @Override
    protected double stepHeight() {
        return hasSwallowedItem() ? 0.0 : super.stepHeight();
    }

    @Override
    protected boolean requiresCustomPersistence() {
        return hasSwallowedItem() || super.requiresCustomPersistence();
    }

    /**
     * {@inheritDoc} A sulfur cube made of size 1 at full health becomes a baby.
     */
    @Override
    public void setSize(final int size, final boolean healed) {
        super.setSize(size, healed);
        if (healed && size == MIN_SIZE && !isBaby()) {
            setBaby(true);
        }
    }

    @Override
    protected float maxHealthFor(final int size) {
        return HEALTH_PER_SIZE * size;
    }

    /**
     * A sulfur cube that spawns is of size 2, or 1 for a baby.
     */
    @Override
    protected void setSpawnSize() {
        setSize(isBaby() ? MIN_SIZE : ADULT_SIZE, true);
    }

    /**
     * A baby that grows up takes the size of an adult, at full health.
     */
    @Override
    protected void onAgeBoundaryReached() {
        super.onAgeBoundaryReached();
        if (!isBaby()) {
            setSize(ADULT_SIZE, true);
        }
    }

    @Override
    protected int splitCount() {
        return SPLIT_COUNT;
    }

    @Override
    protected void setUpSplitCube(final AbstractCubeMob cube) {
        cube.setBaby(true);
    }

    /**
     * The block a sulfur cube holds always falls when it dies.
     */
    @Override
    protected void dropLoot() {
        super.dropLoot();
        dropLootItem(swallowed);
    }

    @Override
    protected boolean canDealDamage() {
        return false;
    }

    @Override
    protected boolean despawnsInPeaceful() {
        return false;
    }

    @Override
    public Sound.Source soundSource() {
        return Sound.Source.NEUTRAL;
    }

    @Override
    protected int experienceReward() {
        return isBaby() ? 0 : 1 + ThreadLocalRandom.current().nextInt(MAX_EXTRA_EXPERIENCE + 1);
    }

    @Override
    protected double baseEyeHeight() {
        return BASE_EYE_HEIGHT;
    }

    @Override
    protected Sound.Type jumpSound() {
        return isTiny() ? SoundEvents.SMALL_SULFUR_CUBE_JUMP : SoundEvents.SULFUR_CUBE_JUMP;
    }

    @Override
    protected Sound.Type squishSound() {
        if (isTiny()) {
            return SoundEvents.SMALL_SULFUR_CUBE_SQUISH;
        }
        return hasSwallowedItem() ? SoundEvents.SULFUR_CUBE_BOUNCE : SoundEvents.SULFUR_CUBE_SQUISH;
    }

    @Override
    protected Sound.Type hurtSound() {
        return isTiny() ? SoundEvents.SMALL_SULFUR_CUBE_HURT : SoundEvents.SULFUR_CUBE_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return isTiny() ? SoundEvents.SMALL_SULFUR_CUBE_DEATH : SoundEvents.SULFUR_CUBE_DEATH;
    }

    @Override
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_PICKUP_TIMER, pickupTimer);
        if (hasSwallowedItem()) {
            try {
                tag.put(TAG_EQUIPMENT, CompoundBinaryTag.builder()
                        .put(EquipmentSlot.BODY.serializedName(), ContainerCodecs.writeItem(swallowed))
                        .build());
            } catch (final IOException e) {
                throw new UncheckedIOException("The block of a sulfur cube could not be saved", e);
            }
            tag.put(TAG_DROP_CHANCES, CompoundBinaryTag.builder()
                    .putFloat(EquipmentSlot.BODY.serializedName(), GUARANTEED_DROP)
                    .build());
        }
    }

    @Override
    public void loadAdditional(final CompoundBinaryTag tag) {
        super.loadAdditional(tag);
        pickupTimer = tag.getInt(TAG_PICKUP_TIMER, 0);
        final BinaryTag body = tag.getCompound(TAG_EQUIPMENT).get(EquipmentSlot.BODY.serializedName());
        setSwallowedItem(body == null ? ItemStack.EMPTY : ContainerCodecs.readItem(body).orElse(ItemStack.EMPTY));
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        super.sendSpawnPackets(connection);
        if (hasSwallowedItem()) {
            connection.send(ClientboundSetEquipmentPacket.of(entityId(), EquipmentSlot.BODY, swallowed));
        }
    }

    /**
     * Makes an adult free to pick things up hop towards the nearest block it could swallow lying
     * within 8 blocks.
     */
    private final class SearchForItemsGoal implements Goal {

        private static final Set<Flag> FLAGS = Set.of(Flag.LOOK);

        private @Nullable ServerItemEntity target;

        @Override
        public int priority() {
            return SEARCH_PRIORITY;
        }

        @Override
        public Set<Flag> flags() {
            return FLAGS;
        }

        @Override
        public boolean canStart() {
            target = isBaby() || pickupTimer > 0 ? null : nearestSwallowable();
            return target != null;
        }

        @Override
        public boolean shouldContinue() {
            return canStart();
        }

        @Override
        public void tick() {
            final ServerItemEntity item = target;
            if (item != null) {
                turnTowards(item.location());
                setDirection(yaw(), true);
            }
        }

        private @Nullable ServerItemEntity nearestSwallowable() {
            final Location at = location();
            final double reachX = halfWidth() + SEARCH_RANGE;
            final double reachY = SEARCH_RANGE;
            final List<ServerItemEntity> found = new ArrayList<>();
            serverWorld().entityManager().forEachNear(at.chunk(), reachX + 1.0, entity -> {
                if (entity instanceof final ServerItemEntity drop && !drop.isRemoved() && drop.pickupDelay() == 0
                        && isSwallowable(drop.item())) {
                    final Location other = drop.location();
                    if (Math.abs(other.x() - at.x()) < reachX && Math.abs(other.z() - at.z()) < reachX
                            && other.y() < at.y() + height() + reachY && at.y() - reachY < other.y() + ITEM_HEIGHT) {
                        found.add(drop);
                    }
                }
            });
            return found.stream().min(Comparator.comparingDouble(SulfurCubeImpl.this::distanceSqTo)).orElse(null);
        }
    }
}
