package fr.euphyllia.fidorial.server.entity.mob.monster.cube;

import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.ai.Targeting;
import fr.euphyllia.fidorial.server.entity.ai.goal.TemptGoal;
import fr.euphyllia.fidorial.server.entity.mob.AbstractAgeableMob;
import fr.euphyllia.fidorial.server.entity.mob.MobAttribute;
import fr.euphyllia.fidorial.server.entity.mob.monster.LocalDifficulty;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.events.mob.CubeSplitEventImpl;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.entity.mob.monster.CubeMob;
import fr.fidorial.event.mob.CubeSplitEvent;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.MobEffectKeys;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * A cube that hops around: the slime and its kin. It does not walk along paths: it faces a way,
 * then hops that way now and then, a little more often when it goes after something. Its size,
 * from 1 to 127, sets how big it is, its health, its speed and how loud it is; a big one splits
 * into cubes of half its size once dead, 2 to 4 unless a kind says otherwise.
 *
 * <p>Like every mob that grows up, a cube has an age; a baby is half as big as its size says.</p>
 *
 * <p>A cube that may hurt hurts the players that touch it, as long as it can see them.</p>
 */
public abstract class AbstractCubeMob extends AbstractAgeableMob implements Category.Monster, CubeMob {

    public static final int MAX_NATURAL_SIZE = 4;

    private static final int MD_SIZE = 18;
    private static final double FOLLOW_RANGE = 16.0;
    private static final double FOLLOW_RANGE_SPREAD = 0.11485;
    private static final Key RANDOM_SPAWN_BONUS = Key.key("random_spawn_bonus");

    /**
     * How far around a player a cube touches them: the box of the player grown by this much across
     * and by {@link #TOUCH_REACH_Y} up and down.
     */
    private static final double TOUCH_REACH = 1.0;
    private static final double TOUCH_REACH_Y = 0.5;

    /**
     * How far a cube turns each tick towards the way it wants to go, in degrees.
     */
    private static final float MAX_TURN = 90.0f;

    /**
     * How big a baby cube is, next to an adult of the same size.
     */
    private static final double BABY_SCALE = 0.5;
    /**
     * How far a cube turns each tick towards what it goes after, in degrees.
     */
    private static final float TURN_SPEED = 10.0f;

    private static final String TAG_SIZE = "Size";
    private static final String TAG_WAS_ON_GROUND = "wasOnGround";

    private final MobAttribute followRangeAttribute = new MobAttribute(Key.key("follow_range"), FOLLOW_RANGE);

    private int size = MIN_SIZE;
    private boolean wasOnGround;

    private float wantedYaw;
    private int jumpDelay;
    private boolean aggressive;
    private boolean wantsToMove;
    private double wantedSpeed;

    /**
     * @param spawned {@code true} for a cube that spawns, which gets the random traits of one, a
     *                random size first; {@code false} for one read back or split off another
     */
    protected AbstractCubeMob(final int entityId, final EntityType type, final Location location, final boolean spawned) {
        super(entityId, UUID.randomUUID(), type, location, 1f, false);
        // the way it first wants to go comes out of its yaw read as radians
        this.wantedYaw = (float) (180.0 * yaw() / Math.PI);
        goals.add(new FloatGoal());
        goals.add(new RandomDirectionGoal());
        goals.add(new KeepOnJumpingGoal());
        if (spawned) {
            // every mob that spawns notices its targets from a little closer or further than the others
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            final double first = random.nextDouble();
            final double second = random.nextDouble();
            followRangeAttribute.put(RANDOM_SPAWN_BONUS, FOLLOW_RANGE_SPREAD * (first - second),
                    MobAttribute.Operation.ADD_MULTIPLIED_BASE);
            setSpawnSize();
        } else {
            resize(MIN_SIZE, true);
        }
    }

    /**
     * {@return a new cube of the same kind, not yet in the world}, as one split off another
     */
    protected abstract AbstractCubeMob newSplitCube(int entityId, Location location);

    protected abstract Sound.Type jumpSound();

    protected abstract Sound.Type squishSound();

    /**
     * {@return the size of this cube, from 1 to 127}
     */
    @Override
    public final int size() {
        return size;
    }

    /**
     * {@return {@code true} for a cube of size 1}
     */
    @Override
    public final boolean isTiny() {
        return size <= MIN_SIZE;
    }

    /**
     * Gives this cube a size, kept between 1 and 127: it grows or shrinks around where it stands, and
     * its health, speed and what depends on its size follow.
     *
     * @param healed {@code true} to give it its full new health
     */
    @Override
    public void setSize(final int size, final boolean healed) {
        resize(size, healed);
    }

    private void resize(final int size, final boolean healed) {
        final int actual = Math.clamp(size, MIN_SIZE, MAX_SIZE);
        final boolean changed = actual != this.size;
        this.size = actual;
        setMaxHealth(maxHealthFor(actual));
        if (healed) {
            setHealth(maxHealth());
        }
        if (changed) {
            sendToTrackers(ClientboundSetEntityMetadataPacket.of(entityId(),
                    ClientboundSetEntityMetadataPacket.Entry.varInt(MD_SIZE, actual)));
        }
    }

    /**
     * {@return the health of a cube of a size}: its size squared
     */
    protected float maxHealthFor(final int size) {
        return size * size;
    }

    /**
     * Gives a cube that spawns a random size: 1, 2 or 4, the bigger more often where the difficulty
     * is high.
     */
    protected void setSpawnSize() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        int scale = random.nextInt(3);
        final Location at = location();
        if (scale < 2 && random.nextFloat() < 0.5f
                * LocalDifficulty.at(serverWorld(), (int) Math.floor(at.x()), (int) Math.floor(at.z())).specialMultiplier()) {
            scale++;
        }
        setSize(1 << scale, true);
    }

    @Override
    public double movementSpeed() {
        return 0.2f + 0.1f * size;
    }

    @Override
    protected double defaultFollowRange() {
        return followRangeAttribute.value();
    }

    @Override
    protected double adultWidth() {
        return type().width() * size;
    }

    protected double adultHeight() {
        return type().height() * size;
    }

    @Override
    public double eyeHeight() {
        return baseEyeHeight() * size * (isBaby() ? BABY_SCALE : 1.0);
    }

    /**
     * {@return the height of the eyes of a cube of size 1}
     */
    protected abstract double baseEyeHeight();

    @Override
    protected boolean despawnsInPeaceful() {
        return true;
    }

    /**
     * {@return {@code true} if this cube hurts what it touches}: any cube but a tiny one
     */
    protected boolean canDealDamage() {
        return !isTiny();
    }

    /**
     * {@return how long a cube waits on the ground before its next hop, in ticks}
     */
    protected int jumpDelayTicks() {
        return ThreadLocalRandom.current().nextInt(20) + 10;
    }

    @Override
    protected float soundVolume() {
        return 0.4f * size;
    }

    /**
     * {@return the pitch of the hops of this cube}: higher for a tiny one
     */
    protected float soundPitch() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final float first = random.nextFloat();
        final float second = random.nextFloat();
        return ((first - second) * 0.2f + 1.0f) * (isTiny() ? 1.4f : 0.8f);
    }

    @Override
    public void tick(final long currentTick) {
        if (isRemoved()) {
            return;
        }
        super.tick(currentTick);
        if (isRemoved() || isDead()) {
            return;
        }
        if (onGround() && !wasOnGround) {
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            final float first = random.nextFloat();
            final float second = random.nextFloat();
            playSound(squishSound(), soundVolume(), ((first - second) * 0.2f + 1.0f) / 0.8f);
        }
        wasOnGround = onGround();
        if (canDealDamage()) {
            hurtTouchingPlayers();
        }
        checkDespawn();
    }

    /**
     * Turns the cube towards the way it wants to go, then, when a goal asked it to move this tick,
     * hops that way once its delay ran out, and keeps going while in the air.
     */
    @Override
    protected void tickMoveControl() {
        setRotation(rotateTowards(yaw(), wantedYaw, MAX_TURN), 0f);
        if (!wantsToMove) {
            return;
        }
        wantsToMove = false;
        final double speed = wantedSpeed * movementSpeed();
        if (!onGround()) {
            setForwardSpeed(speed);
            return;
        }
        if (jumpDelay-- > 0) {
            return;
        }
        jumpDelay = jumpDelayTicks();
        if (aggressive) {
            jumpDelay /= 3;
        }
        setForwardSpeed(speed);
        requestJump();
        playSound(jumpSound(), soundVolume(), soundPitch());
    }

    /**
     * Sets the way this cube wants to go.
     *
     * @param aggressive {@code true} to hop three times as often, as after a target
     */
    protected final void setDirection(final float yaw, final boolean aggressive) {
        this.wantedYaw = yaw;
        this.aggressive = aggressive;
    }

    /**
     * Makes this cube move this tick.
     *
     * @param speed how fast, compared to its movement speed
     */
    protected final void setWantedMovement(final double speed) {
        this.wantedSpeed = speed;
        this.wantsToMove = true;
    }

    private void hurtTouchingPlayers() {
        for (final ServerPlayer player : server().players().online()) {
            if (isTouchedBy(player)) {
                dealDamage(player);
            }
        }
    }

    /**
     * {@return {@code true} if a player touches this cube}: a living player who is no spectator,
     * whose box grown by a block across and half a block up and down meets the box of the cube
     */
    protected final boolean isTouchedBy(final ServerPlayer player) {
        if (!player.world().equals(world()) || player.isDead() || player.gameMode() == GameMode.SPECTATOR) {
            return false;
        }
        final Location at = location();
        final Location other = player.location();
        final double reach = halfWidthOf(player) + TOUCH_REACH + halfWidth();
        return Math.abs(other.x() - at.x()) < reach && Math.abs(other.z() - at.z()) < reach
                && other.y() - TOUCH_REACH_Y < at.y() + height()
                && at.y() < other.y() + boxHeightOf(player) + TOUCH_REACH_Y;
    }

    /**
     * Hurts a living entity this cube touches, if it can reach and see it.
     */
    protected final void dealDamage(final AbstractLivingEntity target) {
        if (isDead() || !isWithinMeleeRange(target) || !hasLineOfSightTo(target)) {
            return;
        }
        if (server().entities().combat().damage(target, DamageSource.mobAttack(this), attackDamage())) {
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            final float first = random.nextFloat();
            final float second = random.nextFloat();
            playSound(SoundEvents.SLIME_ATTACK, 1.0f, (first - second) * 0.2f + 1.0f);
        }
    }

    /**
     * Once dead, a cube bigger than 1 splits into 2 to 4 cubes of half its size around where it
     * lies, each at full health.
     */
    @Override
    protected void onDeathAnimationFinished() {
        if (size > MIN_SIZE) {
            split();
        }
        super.onDeathAnimationFinished();
    }

    private void split() {
        final int initialCount = splitCount();
        if (initialCount <= 0) {
            return;
        }
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final Location at = location();
        final double offset = width() / 2.0;
        final int halfSize = size / 2;
        final CubeSplitEvent event = server().events().post(new CubeSplitEventImpl(this, initialCount, halfSize));
        if (event.isCancelled()) {
            return;
        }
        final int count = event.count();
        for (int i = 0; i < count; i++) {
            // two by two, around where it lay
            final int column = i % 2;
            final int row = i / 2;
            final double dx = (column - 0.5) * offset;
            final double dz = (row - 0.5) * offset;
            final AbstractCubeMob cube = newSplitCube(server().entities().ids().allocate(),
                    Location.of(at.world(), at.x() + dx, at.y() + 0.5, at.z() + dz, random.nextFloat() * 360.0f, 0f));
            cube.setSize(halfSize, true);
            setUpSplitCube(cube);
            cube.setAbsorptionAmount(absorptionAmount());
            if (isPersistenceRequired()) {
                cube.setPersistenceRequired(true);
            }
            server().entities().spawn(cube);
            for (final MobEffectInstance effect : activeEffects()) {
                cube.addEffect(effect);
            }
        }
    }

    /**
     * {@return how many cubes this one splits into once dead}: 2 to 4
     */
    protected int splitCount() {
        return 2 + ThreadLocalRandom.current().nextInt(3);
    }

    /**
     * Finishes a cube split off this one, already of half its size and at full health.
     */
    protected void setUpSplitCube(final AbstractCubeMob cube) {
    }

    /**
     * Turns this cube towards a point, ten degrees a tick at most.
     */
    protected final void turnTowards(final Location other) {
        final Location at = location();
        final float towards = (float) Math.toDegrees(Math.atan2(other.z() - at.z(), other.x() - at.x())) - 90.0f;
        setRotation(rotateTowards(yaw(), towards, TURN_SPEED), 0f);
    }

    @Override
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
        super.saveAdditional(tag);
        MobAttribute.save(tag, followRangeAttribute);
        tag.putInt(TAG_SIZE, size - 1);
        tag.putBoolean(TAG_WAS_ON_GROUND, wasOnGround);
    }

    @Override
    public void loadAdditional(final CompoundBinaryTag tag) {
        // a cube read back keeps nothing of what was rolled when it was created
        followRangeAttribute.remove(RANDOM_SPAWN_BONUS);
        setSize(tag.getInt(TAG_SIZE, 0) + 1, false);
        super.loadAdditional(tag);
        MobAttribute.load(tag, followRangeAttribute);
        wasOnGround = tag.getBoolean(TAG_WAS_ON_GROUND, false);
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        super.sendSpawnPackets(connection);
        connection.send(ClientboundSetEntityMetadataPacket.of(entityId(),
                ClientboundSetEntityMetadataPacket.Entry.varInt(MD_SIZE, size)));
    }

    /**
     * Makes a cube go after its target: it turns towards it, ten degrees a tick, and hops at it
     * three times as often if it may hurt it. It tires of it after 15 seconds and starts over.
     */
    protected final class AttackGoal implements Goal {

        private static final int TIRED_TICKS = 300;
        private static final Set<Flag> FLAGS = Set.of(Flag.LOOK);

        private final int priority;
        private int growTiredTimer;

        public AttackGoal(final int priority) {
            this.priority = priority;
        }

        @Override
        public int priority() {
            return priority;
        }

        @Override
        public Set<Flag> flags() {
            return FLAGS;
        }

        @Override
        public boolean canStart() {
            final AbstractLivingEntity target = target();
            return target != null && Targeting.canAttack(AbstractCubeMob.this, target);
        }

        @Override
        public void start() {
            growTiredTimer = TIRED_TICKS;
        }

        @Override
        public boolean shouldContinue() {
            final AbstractLivingEntity target = target();
            return target != null && Targeting.canAttack(AbstractCubeMob.this, target) && --growTiredTimer > 0;
        }

        @Override
        public void tick() {
            final AbstractLivingEntity target = target();
            if (target != null) {
                turnTowards(target.location());
            }
            setDirection(yaw(), canDealDamage());
        }
    }

    /**
     * Makes a cube follow a player who holds something it likes: it turns towards them, ten degrees
     * a tick, and hops their way three times as often. It does not move by itself: it only steers
     * the hops that its other goals ask for.
     */
    protected final class CubeTemptGoal extends TemptGoal {

        private static final Set<Flag> FLAGS = Set.of(Flag.LOOK);

        /**
         * @param temptingItems the items the cube follows
         * @param range         how far the cube notices such an item, in blocks
         * @param stopDistance  how close to the player the cube stops steering
         */
        public CubeTemptGoal(final int priority, final Predicate<ItemStack> temptingItems,
                             final double range, final double stopDistance) {
            super(AbstractCubeMob.this, priority, 1.0, temptingItems, range, stopDistance);
        }

        @Override
        public Set<Flag> flags() {
            return FLAGS;
        }

        @Override
        protected void stopNavigation() {
            setWantedMovement(0.0);
        }

        @Override
        protected void navigateTowards(final ServerPlayer player) {
            turnTowards(player.location());
            setDirection(yaw(), true);
        }
    }

    /**
     * Makes a cube in water or lava keep hopping up, and move on a little faster.
     */
    private final class FloatGoal implements Goal {

        private static final Set<Flag> FLAGS = Set.of(Flag.JUMP, Flag.MOVE);
        private static final float JUMP_CHANCE = 0.8f;
        private static final double SPEED = 1.2;

        @Override
        public int priority() {
            return 1;
        }

        @Override
        public Set<Flag> flags() {
            return FLAGS;
        }

        @Override
        public boolean canStart() {
            return isInWater() || isInLava();
        }

        @Override
        public boolean shouldContinue() {
            return canStart();
        }

        @Override
        public void tick() {
            if (ThreadLocalRandom.current().nextFloat() < JUMP_CHANCE) {
                requestJump();
            }
            setWantedMovement(SPEED);
        }
    }

    /**
     * Makes a cube with no target turn a random way every 2 to 5 seconds.
     */
    private final class RandomDirectionGoal implements Goal {

        private static final Set<Flag> FLAGS = Set.of(Flag.LOOK);

        private float chosenYaw;
        private int nextRandomizeTime;

        @Override
        public int priority() {
            return 4;
        }

        @Override
        public Set<Flag> flags() {
            return FLAGS;
        }

        @Override
        public boolean canStart() {
            return target() == null && (onGround() || isInWater() || isInLava() || hasEffect(MobEffectKeys.LEVITATION));
        }

        @Override
        public boolean shouldContinue() {
            return canStart();
        }

        @Override
        public void tick() {
            if (--nextRandomizeTime <= 0) {
                final ThreadLocalRandom random = ThreadLocalRandom.current();
                nextRandomizeTime = 40 + random.nextInt(60);
                chosenYaw = random.nextInt(360);
            }
            setDirection(chosenYaw, false);
        }
    }

    /**
     * Makes a cube keep hopping on: it asks to move every other tick.
     */
    private final class KeepOnJumpingGoal implements Goal {

        private static final Set<Flag> FLAGS = Set.of(Flag.JUMP, Flag.MOVE);

        private boolean skip;

        @Override
        public int priority() {
            return 5;
        }

        @Override
        public Set<Flag> flags() {
            return FLAGS;
        }

        @Override
        public boolean canStart() {
            return true;
        }

        @Override
        public boolean shouldContinue() {
            return true;
        }

        @Override
        public void tick() {
            if (!skip) {
                setWantedMovement(1.0);
            }
            skip = !skip;
        }
    }
}
