package fr.euphyllia.fidorial.server.entity.mob.monster.silverfish;

import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.FloatGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.HurtByTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.MeleeAttackGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.NearestAttackableTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomStrollGoal;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMonster;
import fr.euphyllia.fidorial.server.entity.mob.MobAttribute;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.events.mob.MobBlockChanges;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLevelEventPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.InfestedBlocks;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.entity.mob.monster.Silverfish;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.DamageTypeTagKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The silverfish: a tiny monster that hides in stone. It goes after the players it sees; when
 * something hurts it, it calls the silverfish around and, a second later, wakes those hiding in the
 * blocks nearby. With nothing to do it slips into a stone block next to it, which becomes an
 * infested block. Its steps make no sound.
 */
public final class SilverfishImpl extends AbstractMonster implements Silverfish {

    public static final float MAX_HEALTH = 8f;

    private static final double MOVEMENT_SPEED = 0.25;
    private static final float ATTACK_DAMAGE = 1f;
    private static final double FOLLOW_RANGE = 16.0;
    private static final double FOLLOW_RANGE_SPREAD = 0.11485;

    private static final double HALF_WIDTH = 0.2;
    private static final double HEIGHT = 0.3;
    private static final double EYE_HEIGHT = 0.13;

    /**
     * A silverfish does not spawn by itself within this distance of a player in survival or
     * adventure.
     */
    private static final double PLAYER_NEARBY_DISTANCE = 5.0;
    /**
     * How much a silverfish likes to walk onto a block it could hide in.
     */
    private static final float HOST_WALK_VALUE = 10.0f;

    private static final Key RANDOM_SPAWN_BONUS = Key.key("random_spawn_bonus");

    private final MobAttribute followRangeAttribute = new MobAttribute(Key.key("follow_range"), FOLLOW_RANGE);
    private final WakeUpFriendsGoal wakeUpFriends = new WakeUpFriendsGoal();

    /**
     * Creates a silverfish as one that spawns, with the random traits of one.
     */
    public SilverfishImpl(final int entityId, final Location location) {
        this(entityId, location, true);
    }

    private SilverfishImpl(final int entityId, final Location location, final boolean spawned) {
        super(entityId, UUID.randomUUID(), EntityTypes.SILVERFISH, location, MAX_HEALTH);
        goals.add(new FloatGoal(this, 1));
        goals.add(new ClimbOnTopOfPowderSnowGoal(this, 1));
        goals.add(wakeUpFriends);
        goals.add(new MeleeAttackGoal(this, 4, 1.0, false));
        goals.add(new MergeWithStoneGoal());
        goals.add(new HurtByTargetGoal(this, 1).alertOthers());
        goals.add(new NearestAttackableTargetGoal<>(this, 2, ServerPlayer.class, true));
        if (spawned) {
            finalizeSpawn();
        }
    }

    /**
     * Creates a silverfish coming out of an infested block, without the random traits of one that
     * spawns.
     */
    public static SilverfishImpl leavingBlock(final int entityId, final Location location) {
        return new SilverfishImpl(entityId, location, false);
    }

    /**
     * Gives this silverfish the random traits of one that spawns.
     */
    private void finalizeSpawn() {
        // every mob that spawns notices its targets from a little closer or further than the others
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final double first = random.nextDouble();
        final double second = random.nextDouble();
        followRangeAttribute.put(RANDOM_SPAWN_BONUS, FOLLOW_RANGE_SPREAD * (first - second),
                MobAttribute.Operation.ADD_MULTIPLIED_BASE);
    }

    /**
     * {@return {@code true} if a silverfish may spawn on a block}: on solid ground in any light,
     * unless the world is peaceful, and away from the players in survival or adventure
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        return checkAnyLightMonsterSpawnRules(world, x, y, z)
                && isAwayFromPlayers(world, x, y, z, PLAYER_NEARBY_DISTANCE);
    }

    /**
     * Wakes the silverfish hiding nearby a second from now: something with an attacker hurt this
     * one, or something that always does.
     */
    @Override
    public void onHurt(final DamageSource source, final float amount) {
        super.onHurt(source, amount);
        if (source.causingEntity() != null || server().registries().registry(RegistryKey.DAMAGE_TYPE)
                .isTagged(source.type(), DamageTypeTagKeys.ALWAYS_TRIGGERS_SILVERFISH)) {
            wakeUpFriends.notifyHurt();
        }
    }

    /**
     * {@return how much this silverfish likes to walk to a block}: onto a block it could hide in
     * above all, otherwise where it is darker
     */
    @Override
    public float walkTargetValue(final int x, final int y, final int z) {
        final BlockState ground = BlockView.blockAt(serverWorld(), x, y - 1, z);
        return ground != null && InfestedBlocks.isHost(ground) ? HOST_WALK_VALUE : super.walkTargetValue(x, y, z);
    }

    @Override
    public double movementSpeed() {
        return MOVEMENT_SPEED;
    }

    @Override
    public float attackDamage() {
        return ATTACK_DAMAGE;
    }

    @Override
    protected double defaultFollowRange() {
        return followRangeAttribute.value();
    }

    @Override
    public double height() {
        return HEIGHT;
    }

    @Override
    protected double halfWidth() {
        return HALF_WIDTH;
    }

    @Override
    public double eyeHeight() {
        return EYE_HEIGHT;
    }

    @Override
    protected Sound.Type ambientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override
    protected Sound.Type hurtSound() {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    @Override
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
        super.saveAdditional(tag);
        MobAttribute.save(tag, followRangeAttribute);
    }

    @Override
    public void loadAdditional(final CompoundBinaryTag tag) {
        // a silverfish read back keeps nothing of what was rolled when it was created
        followRangeAttribute.remove(RANDOM_SPAWN_BONUS);
        super.loadAdditional(tag);
        MobAttribute.load(tag, followRangeAttribute);
    }

    private boolean mobGriefing() {
        return world().gameRules().getBoolean(GameRuleKeys.MOB_GRIEFING);
    }

    private boolean owns(final BlockPosition position) {
        return server().workers().regionizer().isOwnedByCurrentThread(world().key(), position.chunk());
    }

    @Override
    public void wakeUpFriends() {
        wakeUpFriends.notifyHurt();
    }

    /**
     * A second after this silverfish was hurt, wakes the silverfish hiding in the blocks around:
     * up to 5 blocks up or down and 10 across, the nearest first, stopping after each with one
     * chance in two. When mobs may change the world, the block breaks and lets its silverfish out;
     * otherwise it only turns back into the block it stood for.
     */
    private final class WakeUpFriendsGoal implements Goal {

        private static final int DELAY_TICKS = 20;
        private static final int VERTICAL_RANGE = 5;
        private static final int HORIZONTAL_RANGE = 10;

        private int lookForFriends;

        void notifyHurt() {
            if (lookForFriends == 0) {
                lookForFriends = DELAY_TICKS;
            }
        }

        @Override
        public int priority() {
            return 3;
        }

        @Override
        public Set<Flag> flags() {
            return Set.of();
        }

        @Override
        public boolean canStart() {
            return lookForFriends > 0;
        }

        @Override
        public boolean shouldContinue() {
            return lookForFriends > 0;
        }

        @Override
        public void tick() {
            lookForFriends--;
            if (lookForFriends > 0) {
                return;
            }
            final ServerWorld world = serverWorld();
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            final Location at = location();
            final int baseX = (int) Math.floor(at.x());
            final int baseY = (int) Math.floor(at.y());
            final int baseZ = (int) Math.floor(at.z());
            // 0, 1, -1, 2, -2…: the nearest offsets first
            for (int dy = 0; dy <= VERTICAL_RANGE && dy >= -VERTICAL_RANGE; dy = (dy <= 0 ? 1 : 0) - dy) {
                for (int dx = 0; dx <= HORIZONTAL_RANGE && dx >= -HORIZONTAL_RANGE; dx = (dx <= 0 ? 1 : 0) - dx) {
                    for (int dz = 0; dz <= HORIZONTAL_RANGE && dz >= -HORIZONTAL_RANGE; dz = (dz <= 0 ? 1 : 0) - dz) {
                        final BlockPosition position = Position.block(baseX + dx, baseY + dy, baseZ + dz);
                        if (!owns(position)) {
                            continue;
                        }
                        final BlockState state = BlockView.blockAt(world, position.blockX(), position.blockY(), position.blockZ());
                        if (state == null || !InfestedBlocks.isInfested(state)) {
                            continue;
                        }
                        wake(world, position, state);
                        if (random.nextBoolean()) {
                            return;
                        }
                    }
                }
            }
        }

        private void wake(final ServerWorld world, final BlockPosition position, final BlockState state) {
            if (mobGriefing()) {
                if (MobBlockChanges.change(SilverfishImpl.this, world, position, state, BlockState.withoutProperties(BlockTypeKeys.AIR))) {
                    world.sendToChunkViewers(position.chunkX(), position.chunkZ(), new ClientboundLevelEventPacket(
                            ClientboundLevelEventPacket.BLOCK_BREAK, position,
                            server().registries().blockStates().networkId(state), false));
                    server().debug().gameEvents().emit(world, GameEventKeys.BLOCK_DESTROY, position);
                    InfestedBlocks.afterBreak(world, position, state, ItemStack.EMPTY);
                }
            } else {
                final BlockState host = InfestedBlocks.host(state);
                if (host != null) {
                    MobBlockChanges.change(SilverfishImpl.this, world, position, state, host);
                }
            }
        }
    }

    /**
     * Makes an idle silverfish wander, now and then slipping into a block next to it it could hide
     * in, when mobs may change the world: the block becomes infested and the silverfish vanishes
     * in a puff.
     */
    private final class MergeWithStoneGoal implements Goal {

        private static final int PRIORITY = 5;
        private static final int MERGE_CHANCE = 10;
        private static final int STROLL_INTERVAL = 10;
        private static final Set<Flag> FLAGS = Set.of(Flag.MOVE);
        private static final int[][] DIRECTIONS = {{0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}};

        private final RandomStrollGoal stroll = new RandomStrollGoal(SilverfishImpl.this, PRIORITY, SilverfishImpl.this::movementSpeed)
                .interval(STROLL_INTERVAL);
        private @Nullable BlockPosition mergeInto;

        @Override
        public int priority() {
            return PRIORITY;
        }

        @Override
        public Set<Flag> flags() {
            return FLAGS;
        }

        @Override
        public boolean canStart() {
            mergeInto = null;
            if (target() != null || navigation().isNavigating()) {
                return false;
            }
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            if (mobGriefing() && serverWorld().configuration().mobs().silverfish().hidesInBlocks()
                    && random.nextInt(MERGE_CHANCE) == 0) {
                final int[] direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
                final Location at = location();
                final BlockPosition position = Position.block(
                        (int) Math.floor(at.x()) + direction[0],
                        (int) Math.floor(at.y() + 0.5) + direction[1],
                        (int) Math.floor(at.z()) + direction[2]);
                final BlockState state = BlockView.blockAt(serverWorld(), position.blockX(), position.blockY(), position.blockZ());
                if (owns(position) && state != null && InfestedBlocks.isHost(state)) {
                    mergeInto = position;
                    return true;
                }
            }
            return stroll.canStart();
        }

        @Override
        public boolean shouldContinue() {
            return mergeInto == null && stroll.shouldContinue();
        }

        @Override
        public void start() {
            final BlockPosition position = mergeInto;
            if (position == null) {
                stroll.start();
                return;
            }
            final ServerWorld world = serverWorld();
            final BlockState state = BlockView.blockAt(world, position.blockX(), position.blockY(), position.blockZ());
            final BlockState infested = state == null ? null : InfestedBlocks.infested(state);
            if (state != null && infested != null && MobBlockChanges.change(SilverfishImpl.this, world, position, state, infested)) {
                spawnAnimation();
                server().entities().despawn(SilverfishImpl.this);
            }
        }

        @Override
        public void stop() {
            if (mergeInto == null) {
                stroll.stop();
            }
        }

        @Override
        public void tick() {
            if (mergeInto == null) {
                stroll.tick();
            }
        }
    }
}
