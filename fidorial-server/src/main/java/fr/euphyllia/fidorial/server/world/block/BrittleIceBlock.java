package fr.euphyllia.fidorial.server.world.block;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.farming.FarmingEffects;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.EntityTypeTagKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.registry.keys.SoundEventKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockProperties;
import fr.fidorial.world.redstone.RedstoneWorld;

/**
 * Brittle ice, which water turns into next to blue ice: it cracks under the living entities that
 * walk on it, one step at most every {@value #TICK_DELAY} ticks, and gives way once cracked three
 * times, nothing left behind. Giving way, it cracks the brittle ice around it one step further.
 *
 * <p>Frostbites and strays, the entities of the {@code does_not_break_brittle_ice} tag, walk on it
 * safely; other mobs only crack it where the {@code mob_griefing} game rule lets them. A scheduled
 * tick of the block marks the time it rests between two cracks, and does nothing else.</p>
 */
public final class BrittleIceBlock {

    /**
     * How many ticks brittle ice rests between two cracks.
     */
    public static final int TICK_DELAY = 24;

    private static final int MAX_AGE = 3;

    /**
     * The level event of brittle ice starting to crack: its particles, the data being the state of
     * the block.
     */
    private static final int FIRST_CRACK = 2014;

    private BrittleIceBlock() {
        throw new UnsupportedOperationException("BrittleIceBlock cannot be instantiated.");
    }

    /**
     * Cracks brittle ice a living entity walks on, unless the ice still rests from its last crack.
     *
     * @param world    the world of the ice
     * @param position where the ice is, owned by the calling thread
     * @param entity   the entity walking on it
     */
    public static void walkedOn(final ServerWorld world, final BlockPosition position, final AbstractLivingEntity entity) {
        if (!FidorialServer.isInitialized() || !cracksUnder(world, entity)
                || world.ticks().hasBlockTick(position, BlockTypeKeys.BRITTLE_ICE)) {
            return;
        }
        final RedstoneWorld view = FidorialServer.getInstance().worlds().redstone().world(world);
        final BlockData state = view.blockAt(position);
        if (state == null || !state.key().equals(BlockTypeKeys.BRITTLE_ICE)) {
            return;
        }
        if (crack(world, view, position, state)) {
            crackAround(world, view, position);
        }
        world.ticks().scheduleBlockTick(position, BlockTypeKeys.BRITTLE_ICE, TICK_DELAY);
    }

    private static boolean cracksUnder(final ServerWorld world, final AbstractLivingEntity entity) {
        if (FidorialServer.getInstance().registries().registry(RegistryKey.ENTITY_TYPE)
                .isTagged(entity.type().key(), EntityTypeTagKeys.DOES_NOT_BREAK_BRITTLE_ICE)) {
            return false;
        }
        return entity instanceof ServerPlayer || world.gameRules().getBoolean(GameRuleKeys.MOB_GRIEFING);
    }

    /**
     * Cracks the brittle ice around a block that gave way one step further, letting each crack rest
     * unless it gave way too.
     */
    private static void crackAround(final ServerWorld world, final RedstoneWorld view, final BlockPosition position) {
        for (final BlockFace face : BlockFace.values()) {
            final BlockPosition neighbour = position.relative(face);
            if (!world.scheduler().isOwnedByCurrentThread(world.key(), neighbour.chunk())) {
                continue;
            }
            final BlockData state = view.blockAt(neighbour);
            if (state != null && state.key().equals(BlockTypeKeys.BRITTLE_ICE) && !crack(world, view, neighbour, state)) {
                world.ticks().scheduleBlockTick(neighbour, BlockTypeKeys.BRITTLE_ICE, TICK_DELAY);
            }
        }
    }

    /**
     * Cracks brittle ice one step, or breaks it when it cracked three times already.
     *
     * @return {@code true} if the ice gave way
     */
    private static boolean crack(final ServerWorld world, final RedstoneWorld view, final BlockPosition position,
                                 final BlockData state) {
        final int age = age(state);
        if (age >= MAX_AGE) {
            view.destroyBlock(position);
            return true;
        }
        view.setBlock(position, state.with(BlockProperties.AGE_0_3, age + 1), RedstoneWorld.Update.SHAPES);
        FarmingEffects.playSound(world, position, SoundEventKeys.BLOCK_BRITTLE_ICE_CRACK, 1.0f, 1.0f, null);
        if (age == 0) {
            FarmingEffects.levelEvent(world, FIRST_CRACK, position, state.networkId());
        }
        FarmingEffects.gameEvent(world, GameEventKeys.BLOCK_CHANGE, position);
        return false;
    }

    private static int age(final BlockData state) {
        return state.getOrDefault(BlockProperties.AGE_0_3, 0);
    }
}
