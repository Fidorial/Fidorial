package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLevelEventPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.world.block.BlockType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Makes a mob graze: now and then, standing in grass or on a grass block,
 * it lowers its head for two seconds and eats: the grass is gone, or the grass block
 * turns to dirt, unless the {@code mob_griefing} game rule is off. Either way the mob
 * {@linkplain AbstractPathfinderMob#ate() has eaten}.
 */
public final class EatBlockGoal implements Goal {

    private static final int EAT_ANIMATION_TICKS = 40;
    /**
     * The mob eats this many ticks before it raises its head again.
     */
    private static final int EAT_AT_TICKS_LEFT = 4;
    /**
     * Vanilla rolls one in 500, one in 25 for a baby, every other tick.
     */
    private static final int ADULT_CHANCE = 1000;
    private static final int BABY_CHANCE = 50;
    private static final byte ENTITY_EVENT_EAT_GRASS = 10;

    private final AbstractPathfinderMob mob;
    private final int priority;
    private int eatAnimationTick;

    public EatBlockGoal(final AbstractPathfinderMob mob, final int priority) {
        this.mob = mob;
        this.priority = priority;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean canStart() {
        if (ThreadLocalRandom.current().nextInt(mob.isBaby() ? BABY_CHANCE : ADULT_CHANCE) != 0) {
            return false;
        }
        return canEatHere();
    }

    /**
     * {@return {@code true} if the mob stands in grass it can eat, or on a grass block}
     */
    public boolean canEatHere() {
        final ServerWorld world = mob.serverWorld();
        final BlockPosition feet = feet();
        return isEdible(BlockView.blockAt(world, feet.blockX(), feet.blockY(), feet.blockZ()))
                || isGrassBlock(BlockView.blockAt(world, feet.blockX(), feet.blockY() - 1, feet.blockZ()));
    }

    @Override
    public boolean shouldContinue() {
        return eatAnimationTick > 0;
    }

    @Override
    public void start() {
        eatAnimationTick = EAT_ANIMATION_TICKS;
        mob.sendToTrackers(new ClientboundEntityEventPacket(mob.entityId(), ENTITY_EVENT_EAT_GRASS));
        mob.navigation().stop();
    }

    @Override
    public void stop() {
        eatAnimationTick = 0;
    }

    /**
     * {@return for how many more ticks the mob keeps its head down}
     */
    public int eatAnimationTick() {
        return eatAnimationTick;
    }

    @Override
    public void tick() {
        eatAnimationTick = Math.max(0, eatAnimationTick - 1);
        if (eatAnimationTick != EAT_AT_TICKS_LEFT) {
            return;
        }
        final ServerWorld world = mob.serverWorld();
        final boolean griefing = world.gameRuleValues().getBoolean(GameRuleKeys.MOB_GRIEFING);
        final BlockPosition feet = feet();
        final BlockState inside = BlockView.blockAt(world, feet.blockX(), feet.blockY(), feet.blockZ());
        if (isEdible(inside)) {
            if (griefing) {
                replace(world, feet, inside, BlockTypeKeys.AIR);
            }
            mob.ate();
            return;
        }
        final BlockPosition below = Position.block(feet.blockX(), feet.blockY() - 1, feet.blockZ());
        final BlockState ground = BlockView.blockAt(world, below.blockX(), below.blockY(), below.blockZ());
        if (isGrassBlock(ground)) {
            if (griefing) {
                replace(world, below, ground, BlockTypeKeys.DIRT);
            }
            mob.ate();
        }
    }

    private BlockPosition feet() {
        final Location at = mob.location();
        return Position.block((int) Math.floor(at.x()), (int) Math.floor(at.y()), (int) Math.floor(at.z()));
    }

    private static boolean isEdible(final BlockState state) {
        return state != null && BlockView.isTagged(BlockTypeTagKeys.EDIBLE_FOR_SHEEP, state);
    }

    private static boolean isGrassBlock(final BlockState state) {
        return state != null && state.name().equals(BlockTypeKeys.GRASS_BLOCK);
    }

    /**
     * Replaces a block the mob ate, with the particles and the sound of a broken block.
     */
    private void replace(final ServerWorld world, final BlockPosition pos, final BlockState eaten,
                         final TypedKey<BlockType> replacement) {
        final var server = mob.server();
        server.players().broadcastNear(world, pos.blockX() + 0.5, pos.blockY() + 0.5, pos.blockZ() + 0.5,
                new ClientboundLevelEventPacket(ClientboundLevelEventPacket.BLOCK_BREAK, pos,
                        server.registries().blockStates().networkId(eaten), false), null);
        server.worlds().blockEdits().set(world, pos, BlockState.withoutProperties(replacement), true);
    }
}
