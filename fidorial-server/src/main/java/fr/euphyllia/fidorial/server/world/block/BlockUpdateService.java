package fr.euphyllia.fidorial.server.world.block;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.util.annotations.NeedsToBeRevisited;
import fr.euphyllia.fidorial.server.world.BlockEditService;
import fr.euphyllia.fidorial.server.world.BlockStateRegistry;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.interaction.FidorialBlockInteractionContext;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.GameMode;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.block.BlockAccess;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.interaction.InteractionResult;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public final class BlockUpdateService {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(BlockUpdateService.class);

    private static final int MAX_UPDATE_DEPTH = 512;

    private static final BlockFace[] FACES = BlockFace.values();

    private final BlockEditService edits;
    private final BlockStateRegistry states;
    private final FidorialBlockRegistry blocks;
    private final DropSink drops;
    private final ThreadLocal<int[]> depth = ThreadLocal.withInitial(() -> new int[1]);

    public BlockUpdateService(final BlockEditService edits,
                              final BlockStateRegistry states,
                              final FidorialBlockRegistry blocks,
                              final DropSink drops) {
        this.edits = edits;
        this.states = states;
        this.blocks = blocks;
        this.drops = drops;
    }

    public static DropSink loggingDrops() {
        return new LoggingDropSink();
    }

    public BlockAccess access(final ServerWorld world, final @Nullable ServerPlayer actor) {
        return new WorldAccess(world, actor);
    }

    public BlockAccess access(final ServerWorld world) {
        return access(world, null);
    }

    public @Nullable BlockBehaviour behaviour(final BlockState state) {
        return blocks.explicitBehaviour(state.name());
    }

    public boolean isRandomlyTicking(final BlockState state) {
        final BlockBehaviour behaviour = behaviour(state);
        if (behaviour == null) {
            return false;
        }
        final BlockData data = states.resolve(state);
        return data != null && behaviour.isRandomlyTicking(data);
    }

    public void randomTick(final ServerWorld world, final BlockPos pos, final BlockState state) {
        final BlockBehaviour behaviour = behaviour(state);
        final BlockData data = behaviour == null ? null : states.resolve(state);
        if (behaviour == null || data == null) {
            return;
        }
        try {
            behaviour.randomTick(data, access(world), pos, ThreadLocalRandom.current());
        } catch (final Throwable t) {
            LOGGER.error("Random tick of {} at {} failed", state.name().asString(), pos, t);
        }
    }

    public boolean breaksInstantly(final BlockState state) {
        final BlockBehaviour behaviour = behaviour(state);
        final BlockData data = behaviour == null ? null : states.resolve(state);
        return data != null && behaviour.breaksInstantly(data);
    }

    public boolean canSurvive(final ServerWorld world, final BlockPos pos, final BlockState state) {
        final BlockBehaviour behaviour = behaviour(state);
        if (behaviour == null) {
            return true;
        }
        final BlockData data = states.resolve(state);
        return data == null || behaviour.canSurvive(data, access(world), pos);
    }

    public boolean place(final ServerWorld world, final BlockPos pos, final BlockState state, final ServerPlayer placer,
                         final FidorialBlockInteractionContext context) {
        if (!set(world, pos, state, placer)) {
            return false;
        }
        final BlockBehaviour behaviour = behaviour(state);
        final BlockData data = behaviour == null ? null : states.resolve(state);
        if (data != null) {
            final var sound = behaviour.placeSound(data);
            if (sound != null) {
                context.playSoundAt(pos, sound);
            }
        }
        return true;
    }

    public InteractionResult use(final FidorialBlockInteractionContext context) {
        final BlockBehaviour behaviour = behaviour(context.state());
        if (behaviour == null) {
            return InteractionResult.PASS;
        }
        try {
            return behaviour.use(context.block(), context);
        } catch (final Throwable t) {
            LOGGER.error("Interaction with {} at {} failed", context.state().name().asString(), context.pos(), t);
            return InteractionResult.PASS;
        }
    }

    public boolean set(final ServerWorld world, final BlockPos pos, final BlockState state, final @Nullable ServerPlayer actor) {
        if (!ownedHere(world, pos) || !edits.set(world, pos, state)) {
            return false;
        }
        updateNeighbours(world, pos, actor);
        return true;
    }

    public boolean destroy(final ServerWorld world, final BlockPos pos, final boolean dropItems, final @Nullable ServerPlayer actor) {
        if (!ownedHere(world, pos)) {
            return false;
        }
        final BlockState broken = read(world, pos);
        if (broken.isAir()) {
            return false;
        }
        if (!edits.set(world, pos, BlockState.of(BlockTypeKeys.AIR.key()))) {
            return false;
        }
        if (dropItems && (actor == null || actor.gameMode() != GameMode.CREATIVE)) {
            dropLoot(world, pos, broken);
        }
        updateNeighbours(world, pos, actor);
        return true;
    }

    private void dropLoot(final ServerWorld world, final BlockPos pos, final BlockState broken) {
        final BlockBehaviour behaviour = behaviour(broken);
        final BlockData data = behaviour == null ? null : states.resolve(broken);
        if (data == null) {
            return;
        }
        final List<ItemStack> loot = behaviour.drops(data, ThreadLocalRandom.current());
        if (!loot.isEmpty()) {
            drops.drop(world, pos, loot);
        }
    }

    private void updateNeighbours(final ServerWorld world, final BlockPos pos, final @Nullable ServerPlayer actor) {
        final int[] counter = depth.get();
        if (counter[0] >= MAX_UPDATE_DEPTH) {
            LOGGER.warn("Neighbour updates cut at {}: more than {} chained changes", pos, MAX_UPDATE_DEPTH);
            return;
        }
        counter[0]++;
        try {
            final BlockData changed = dataOrAir(read(world, pos));
            for (final BlockFace face : FACES) {
                updateNeighbour(world, pos.relative(face), face.opposite(), changed, actor);
            }
        } finally {
            counter[0]--;
        }
    }

    private void updateNeighbour(final ServerWorld world,
                                 final BlockPos pos,
                                 final BlockFace towardsChange,
                                 final BlockData changed,
                                 final @Nullable ServerPlayer actor) {
        if (!ownedHere(world, pos)) {
            return;
        }
        final BlockState state = read(world, pos);
        final BlockBehaviour behaviour = behaviour(state);
        final BlockData data = behaviour == null ? null : states.resolve(state);
        if (data == null) {
            return;
        }
        try {
            final BlockAccess access = access(world, actor);
            final BlockData shaped = behaviour.updateShape(data, towardsChange, changed, access, pos);
            if (!shaped.equals(data)) {
                set(world, pos, states.toBlockState(shaped), actor);
            }
            final BlockBehaviour current = shaped.key().equals(data.key()) ? behaviour : blocks.explicitBehaviour(shaped.key());
            if (current != null && !current.canSurvive(shaped, access, pos)) {
                destroy(world, pos, true, actor);
            }
        } catch (final Throwable t) {
            LOGGER.error("Neighbour update of {} at {} failed", state.name().asString(), pos, t);
        }
    }

    private BlockData dataOrAir(final BlockState state) {
        final BlockData data = states.resolve(state);
        if (data != null) {
            return data;
        }
        return Objects.requireNonNull(
                Objects.requireNonNull(Blocks.type(BlockTypeKeys.AIR.key()), "air block type").defaultData(),
                "air block data");
    }

    private static boolean ownedHere(final ServerWorld world, final BlockPos pos) {
        return world.scheduler().isOwnedByCurrentThread(world.key(), ChunkPos.fromBlock(pos.x(), pos.z()));
    }

    private static BlockState read(final ServerWorld world, final BlockPos pos) {
        return FidorialBlockInteractionContext.readState(world, pos);
    }

    @FunctionalInterface
    public interface DropSink {
        void drop(ServerWorld world, BlockPos pos, List<ItemStack> items);
    }

    private static final class LoggingDropSink implements DropSink {
        @Override
        @NeedsToBeRevisited("Spawn item entities once they exist.")
        public void drop(final ServerWorld world, final BlockPos pos, final List<ItemStack> items) {
            for (final ItemStack item : items) {
                LOGGER.info("DROP ITEM : {} count : {} at {}", item.id(), item.count(), pos);
            }
        }
    }

    private final class WorldAccess implements BlockAccess {

        private final ServerWorld world;
        private final @Nullable ServerPlayer actor;

        private WorldAccess(final ServerWorld world, final @Nullable ServerPlayer actor) {
            this.world = world;
            this.actor = actor;
        }

        @Override
        public BlockData blockAt(final BlockPos pos) {
            return dataOrAir(read(world, pos));
        }

        @Override
        public boolean setBlock(final BlockPos pos, final BlockData data) {
            return set(world, pos, states.toBlockState(data), actor);
        }

        @Override
        public boolean destroyBlock(final BlockPos pos, final boolean dropItems) {
            return destroy(world, pos, dropItems, actor);
        }

        @Override
        public void dropItems(final BlockPos pos, final List<ItemStack> items) {
            if (!items.isEmpty()) {
                drops.drop(world, pos, items);
            }
        }

        @Override
        public int lightLevel(final BlockPos pos) {
            return world.lightLevelAt(pos.x(), pos.y(), pos.z());
        }
    }
}
