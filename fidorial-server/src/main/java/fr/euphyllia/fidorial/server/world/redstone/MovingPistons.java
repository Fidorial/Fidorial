package fr.euphyllia.fidorial.server.world.redstone;

import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.ServerWorldManager;
import fr.euphyllia.fidorial.server.world.block.BlockSupport;
import fr.euphyllia.fidorial.server.world.block.blockentity.BlockEntity;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.entity.player.Player;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockEntityTypeKeys;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.scheduler.RegionTickHandler;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.data.Waterlogged;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * The blocks pistons are moving, what vanilla keeps in the block entities of
 * {@code minecraft:moving_piston}: each takes two game ticks to reach its destination, half a block
 * per game tick, and lands on the third.
 *
 * <p>Like vanilla's block entities, they are ticked after the scheduled ticks and the block events
 * of the game tick, in the order they started moving. A block created by a block event therefore
 * moves half a block on that very game tick, and lands two game ticks later.</p>
 *
 * <p>What moves is also written to the block entity of its {@code moving_piston} block, in the
 * vanilla format: clients loading the chunk draw it, and a block caught moving by a save moves on
 * when its chunk is loaded again.</p>
 *
 * <p>The blocks of one region are only ever touched by the thread ticking it.</p>
 */
public final class MovingPistons implements RegionTickHandler {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(MovingPistons.class);

    /**
     * How far a block moves in one game tick, in blocks.
     */
    private static final float PROGRESS_PER_TICK = 0.5f;

    /**
     * What vanilla adds to the distance an entity is pushed, so that it no longer touches the block.
     */
    private static final double PUSH_MARGIN = 0.01;

    private static final String TAG_BLOCK_STATE = "blockState";
    private static final String TAG_NAME = "Name";
    private static final String TAG_PROPERTIES = "Properties";
    private static final String TAG_FACING = "facing";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_EXTENDING = "extending";
    private static final String TAG_SOURCE = "source";

    private final ServerWorldManager worlds;
    private final ThreadedRegionRegionizer regionizer;
    private final FidorialRedstoneRegistry registry;
    private final Function<ServerWorld, RedstoneWorld> redstone;
    private final Map<RegionKey, RegionBlocks> regions = new ConcurrentHashMap<>();

    /**
     * @param worlds     the worlds of the server
     * @param regionizer who owns which chunk
     * @param registry   the redstone behaviours, which shape a block when it lands
     * @param redstone   gives the redstone of a world, as the engine in effect drives it
     */
    public MovingPistons(final ServerWorldManager worlds, final ThreadedRegionRegionizer regionizer,
                         final FidorialRedstoneRegistry registry, final Function<ServerWorld, RedstoneWorld> redstone) {
        this.worlds = worlds;
        this.regionizer = regionizer;
        this.registry = registry;
        this.redstone = redstone;
    }

    private static BlockData air() {
        return Objects.requireNonNull(Objects.requireNonNull(Blocks.type(BlockTypeKeys.AIR), "minecraft:air is not registered").defaultData());
    }

    private static boolean isMovingPiston(final @Nullable BlockData state) {
        return state != null && state.key().equals(BlockTypeKeys.MOVING_PISTON);
    }

    @Override
    public void tick(final Key world, final int sectionX, final int sectionZ, final long currentTick) {
        if (!ThreadedRegionRegionizer.isGameTick()) {
            return; // frozen: what moves waits where it is
        }
        final RegionKey key = new RegionKey(world, sectionX, sectionZ);
        final RegionBlocks blocks = regions.get(key);
        if (blocks == null) {
            return;
        }
        final ServerWorld serverWorld = worlds.world(world);
        if (serverWorld == null) {
            blocks.clear();
            regions.remove(key, blocks);
            return;
        }
        blocks.tick(serverWorld);
        if (blocks.isEmpty()) {
            blocks.releaseTicket();
            regions.remove(key, blocks);
        }
    }

    /**
     * Starts moving a block: the {@code moving_piston} block standing in for it is in place at its
     * destination, which it reaches two game ticks later.
     *
     * @param world        the world
     * @param position     where the block is going, and where its {@code moving_piston} block is
     * @param moved        the state that is moving, the one that lands
     * @param facing       the side the piston moving it faces
     * @param extending    {@code true} if the piston is extending, the block then moving towards
     *                     {@code facing}; {@code false} if it is retracting, the block moving the
     *                     other way
     * @param sourcePiston {@code true} for a part of the piston itself: its head coming out, or its
     *                     base while the head comes back in
     * @throws IllegalStateException if the calling thread does not own {@code position}
     */
    public void start(final ServerWorld world, final BlockPosition position, final BlockData moved, final BlockFace facing,
                      final boolean extending, final boolean sourcePiston) {
        final RegionBlocks blocks = ownedBlocks(world, position, true);
        if (blocks == null) {
            throw new IllegalStateException("A piston can only move a block of the region the calling thread owns, not " + position);
        }
        final MovingBlock block = new MovingBlock(position, moved, facing, extending, sourcePiston, 0.0f);
        blocks.add(block);
        save(world, block);
    }

    /**
     * {@return the block moving to a position, {@code null} when none is, or when the calling thread
     * does not own the position}
     *
     * @param world    the world
     * @param position where the {@code moving_piston} block is
     */
    public @Nullable MovingBlock at(final World world, final BlockPosition position) {
        if (!(world instanceof final ServerWorld serverWorld)) {
            return null;
        }
        final RegionBlocks blocks = ownedBlocks(serverWorld, position, false);
        return blocks == null ? null : blocks.get(position);
    }

    /**
     * {@return {@code true} if a block is too early in its move for the sticky piston pushing it to
     * pull it back}: what makes that piston let go of its block on a short pulse. As in vanilla, that
     * is always, but for a block that moved a game tick ago and waits its turn while the moving blocks
     * are ticked.
     *
     * @param world the world
     * @param block a moving block
     */
    public boolean isStartingToMove(final World world, final MovingBlock block) {
        if (block.lastProgress < PROGRESS_PER_TICK || !(world instanceof final ServerWorld serverWorld)) {
            return true;
        }
        final RegionBlocks blocks = ownedBlocks(serverWorld, block.position, false);
        return blocks == null || !blocks.ticking || block.lastPass == blocks.pass;
    }

    /**
     * Makes a moving block reach its destination at once: what a piston does to its head, and a
     * sticky piston to the block it pushes, when it retracts before they arrived. The head coming out of a piston disappears instead.
     *
     * @param world    the redstone of the world
     * @param position where the {@code moving_piston} block is
     */
    public void finish(final RedstoneWorld world, final BlockPosition position) {
        final MovingBlock block = at(world.world(), position);
        if (block == null || !(world.world() instanceof final ServerWorld serverWorld)) {
            return;
        }
        forget(serverWorld, position);
        if (!isMovingPiston(world.blockAt(position))) {
            return;
        }
        final BlockData state = block.sourcePiston ? air() : shapedByNeighbours(world, position, block.moved);
        world.setBlock(position, state, RedstoneWorld.Update.NEIGHBORS);
        world.neighborChanged(position, state.key());
    }

    /**
     * Forgets the block moving to a position, without landing it: its {@code moving_piston} block
     * was replaced.
     *
     * @param world    the world
     * @param position where the {@code moving_piston} block was
     */
    public void forget(final World world, final BlockPosition position) {
        if (!(world instanceof final ServerWorld serverWorld)) {
            return;
        }
        final RegionBlocks blocks = ownedBlocks(serverWorld, position, false);
        if (blocks != null) {
            blocks.remove(position);
        }
    }

    /**
     * Moves on the blocks a chunk was saved with while they were moving.
     *
     * @param world  the world of the chunk
     * @param column a chunk that was just read from disk
     */
    public void resume(final ServerWorld world, final ChunkColumn column) {
        if (column.blockEntityCount() == 0) {
            return;
        }
        final List<MovingBlock> saved = new ArrayList<>();
        for (final BlockEntity blockEntity : column.blockEntities()) {
            if (blockEntity.type().equals(BlockEntityTypeKeys.PISTON)) {
                final BlockPosition position = Position.block(
                        (column.chunkX() << 4) + blockEntity.localX(), blockEntity.y(), (column.chunkZ() << 4) + blockEntity.localZ());
                saved.add(load(position, blockEntity.dataOrEmpty()));
            }
        }
        if (saved.isEmpty()) {
            return;
        }
        final ChunkPos chunk = new ChunkPos(column.chunkX(), column.chunkZ());
        regionizer.execute(world.key(), chunk, () -> {
            final RedstoneWorld view = redstone.apply(world);
            for (final MovingBlock block : saved) {
                final RegionBlocks blocks = ownedBlocks(world, block.position, true);
                if (blocks != null && isMovingPiston(view.blockAt(block.position)) && blocks.get(block.position) == null) {
                    blocks.add(block);
                }
            }
        });
    }

    private @Nullable RegionBlocks ownedBlocks(final ServerWorld world, final BlockPosition position, final boolean create) {
        final ChunkPos chunk = position.chunk();
        if (!regionizer.isOwnedByCurrentThread(world.key(), chunk)) {
            return null;
        }
        final RegionKey key = RegionKey.of(world.key(), chunk);
        return create ? regions.computeIfAbsent(key, RegionBlocks::new) : regions.get(key);
    }

    /**
     * Lands a block that reached its destination.
     */
    private void land(final RedstoneWorld world, final MovingBlock block) {
        final BlockPosition position = block.position;
        if (block.moved.isAir()) {
            world.setBlock(position, block.moved, RedstoneWorld.Update.NEIGHBORS);
            return;
        }
        BlockData state = shapedByNeighbours(world, position, block.moved);
        if (state.isAir()) {
            // it cannot stand where it landed: it is put down, then broken
            world.setBlock(position, block.moved, RedstoneWorld.Update.QUIET);
            world.destroyBlock(position);
            return;
        }
        if (state instanceof final Waterlogged waterlogged && waterlogged.isWaterlogged()) {
            state = waterlogged.withWaterlogged(false); // a piston never carries water along
        }
        world.setBlock(position, state, RedstoneWorld.Update.NEIGHBORS);
        world.neighborChanged(position, state.key());
    }

    /**
     * {@return a state as its neighbours shape it}: what makes an observer pulse once it was moved
     */
    private BlockData shapedByNeighbours(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        BlockData shaped = state;
        try {
            for (final BlockFace face : Faces.SHAPE_ORDER) {
                final RedstoneBehaviour behaviour = registry.behaviourOrNull(shaped.key());
                final BlockData neighbor = world.blockAt(position.relative(face));
                if (behaviour == null || neighbor == null) {
                    continue;
                }
                shaped = behaviour.updateShape(world, position, shaped, face, neighbor);
                if (shaped.isAir()) {
                    return shaped;
                }
            }
        } catch (final RuntimeException exception) {
            LOGGER.error("The redstone behaviour of {} failed when it landed at {} in {}", state.key(), position, world.world().key(), exception);
            return state;
        }
        return shaped;
    }

    /**
     * Pushes the entities a moving block ran into out of its way, along its movement. Players are
     * left to their client, which moves them itself.
     */
    private static void pushEntities(final ServerWorld world, final MovingBlock block) {
        if (!BlockSupport.isCollisionShapeFullBlock(block.moved) && !block.moved.key().equals(BlockTypeKeys.PISTON_HEAD)) {
            return;
        }
        final BlockPosition position = block.position;
        final BlockFace movement = block.movement();
        final double behind = 1.0 - block.progress; // how far from its destination the block still is
        final double minX = position.blockX() - movement.dx() * behind;
        final double minY = position.blockY() - movement.dy() * behind;
        final double minZ = position.blockZ() - movement.dz() * behind;
        final List<AbstractEntity> pushed = new ArrayList<>();
        world.entityManager().forEachInChunkRange(position.chunkX(), position.chunkZ(), 1, entity -> {
            if (entity instanceof Player || entity.isRemoved()) {
                return;
            }
            final Location at = entity.location();
            final double half = entity.type().width() / 2.0;
            final double height = entity.type().height();
            if (at.x() + half > minX && at.x() - half < minX + 1.0
                    && at.y() + height > minY && at.y() < minY + 1.0
                    && at.z() + half > minZ && at.z() - half < minZ + 1.0) {
                pushed.add(entity);
            }
        });
        for (final AbstractEntity entity : pushed) {
            final Location at = entity.location();
            final double half = entity.type().width() / 2.0;
            final double overlap = switch (movement) {
                case EAST -> minX + 1.0 - (at.x() - half);
                case WEST -> at.x() + half - minX;
                case UP -> minY + 1.0 - at.y();
                case DOWN -> at.y() + entity.type().height() - minY;
                case SOUTH -> minZ + 1.0 - (at.z() - half);
                case NORTH -> at.z() + half - minZ;
            };
            // never further than the block moved on this game tick
            final double distance = Math.min(overlap, PROGRESS_PER_TICK) + PUSH_MARGIN;
            entity.teleport(Location.of(world, at.x() + movement.dx() * distance, at.y() + movement.dy() * distance,
                    at.z() + movement.dz() * distance, at.yaw(), at.pitch()));
        }
    }

    /**
     * Writes a moving block to the block entity of its {@code moving_piston} block, in the vanilla
     * format.
     */
    private static void save(final ServerWorld world, final MovingBlock block) {
        final CompoundBinaryTag.Builder state = CompoundBinaryTag.builder()
                .putString(TAG_NAME, block.moved.key().key().asString());
        final Map<String, String> properties = block.moved.propertyMap();
        if (!properties.isEmpty()) {
            final CompoundBinaryTag.Builder values = CompoundBinaryTag.builder();
            properties.forEach(values::putString);
            state.put(TAG_PROPERTIES, values.build());
        }
        world.setBlockEntityData(block.position, CompoundBinaryTag.builder()
                .put(TAG_BLOCK_STATE, state.build())
                .putInt(TAG_FACING, block.facing.ordinal())
                .putFloat(TAG_PROGRESS, block.lastProgress)
                .putBoolean(TAG_EXTENDING, block.extending)
                .putBoolean(TAG_SOURCE, block.sourcePiston)
                .build());
    }

    /**
     * Reads a moving block back from its block entity. One that cannot be read lands as air, as in
     * vanilla.
     */
    private static MovingBlock load(final BlockPosition position, final CompoundBinaryTag data) {
        return new MovingBlock(position, loadState(data.getCompound(TAG_BLOCK_STATE)), BlockFace.byId(data.getInt(TAG_FACING)),
                data.getBoolean(TAG_EXTENDING), data.getBoolean(TAG_SOURCE), data.getFloat(TAG_PROGRESS));
    }

    private static BlockData loadState(final CompoundBinaryTag tag) {
        final String name = tag.getString(TAG_NAME);
        if (name.isEmpty()) {
            return air();
        }
        try {
            final TypedKey<BlockType> key = TypedKey.withRegistry(RegistryKey.BLOCK, name);
            final BlockType type = Blocks.type(key);
            BlockData state = type == null ? null : type.defaultData();
            if (state == null) {
                return air();
            }
            for (final Map.Entry<String, ? extends BinaryTag> property : tag.getCompound(TAG_PROPERTIES)) {
                if (property.getValue() instanceof final StringBinaryTag value) {
                    state = state.with(property.getKey(), value.value());
                }
            }
            return state;
        } catch (final InvalidKeyException | IllegalArgumentException exception) {
            return air(); // a block or a property this version does not know
        }
    }

    /**
     * A block a piston is moving.
     */
    public static final class MovingBlock {

        private final BlockPosition position;
        private final BlockData moved;
        private final BlockFace facing;
        private final boolean extending;
        private final boolean sourcePiston;
        /**
         * How far the block is, from {@code 0} at its origin to {@code 1} at its destination.
         */
        private float progress;
        /**
         * Its progress before it last moved.
         */
        private float lastProgress;
        /**
         * The pass of its region it was last ticked on.
         */
        private long lastPass = -1;
        private boolean gone;

        private MovingBlock(final BlockPosition position, final BlockData moved, final BlockFace facing, final boolean extending,
                            final boolean sourcePiston, final float progress) {
            this.position = position;
            this.moved = moved;
            this.facing = facing;
            this.extending = extending;
            this.sourcePiston = sourcePiston;
            this.progress = Math.clamp(progress, 0.0f, 1.0f);
            this.lastProgress = this.progress;
        }

        /**
         * {@return where the block is going, and where its {@code moving_piston} block is}
         */
        public BlockPosition position() {
            return position;
        }

        /**
         * {@return the state that is moving, the one that lands}
         */
        public BlockData moved() {
            return moved;
        }

        /**
         * {@return the side the piston moving the block faces}
         */
        public BlockFace facing() {
            return facing;
        }

        /**
         * {@return {@code true} if the piston moving the block is extending, {@code false} if it is
         * retracting}
         */
        public boolean extending() {
            return extending;
        }

        /**
         * {@return {@code true} for a part of the piston itself: its head, or its base}
         */
        public boolean sourcePiston() {
            return sourcePiston;
        }

        /**
         * {@return the side the block moves towards}
         */
        public BlockFace movement() {
            return extending ? facing : facing.opposite();
        }

        /**
         * {@return how far the block is, from {@code 0} at its origin to {@code 1} at its destination}
         */
        public float progress() {
            return progress;
        }
    }

    /**
     * The moving blocks of one region, only ever touched by the thread ticking it.
     */
    private final class RegionBlocks {

        private final RegionKey key;
        /**
         * In the order they started moving, the order vanilla ticks its block entities in.
         */
        private final LinkedHashMap<BlockPosition, MovingBlock> blocks = new LinkedHashMap<>();
        private long pass;
        private boolean ticking;
        private @Nullable ChunkPos ticket;

        RegionBlocks(final RegionKey key) {
            this.key = key;
        }

        void add(final MovingBlock block) {
            final MovingBlock replaced = blocks.put(block.position, block);
            if (replaced != null) {
                replaced.gone = true;
            }
            if (ticket == null) {
                // keeps the region ticking until every block landed
                ticket = block.position.chunk();
                regionizer.addTicket(key.dimension(), ticket);
            }
        }

        @Nullable MovingBlock get(final BlockPosition position) {
            return blocks.get(position);
        }

        void remove(final BlockPosition position) {
            final MovingBlock removed = blocks.remove(position);
            if (removed != null) {
                removed.gone = true;
            }
        }

        void tick(final ServerWorld world) {
            if (blocks.isEmpty()) {
                return;
            }
            final RedstoneWorld view = redstone.apply(world);
            pass++;
            ticking = true;
            try {
                // a block landing may make others land, or be forgotten: they are skipped
                for (final MovingBlock block : List.copyOf(blocks.values())) {
                    if (!block.gone) {
                        tick(world, view, block);
                    }
                }
            } finally {
                ticking = false;
            }
        }

        private void tick(final ServerWorld world, final RedstoneWorld view, final MovingBlock block) {
            block.lastPass = pass;
            if (!isMovingPiston(view.blockAt(block.position))) {
                remove(block.position); // replaced, or its chunk was unloaded: it moves on once loaded again
                return;
            }
            block.lastProgress = block.progress;
            if (block.lastProgress >= 1.0f) {
                remove(block.position);
                try {
                    land(view, block);
                } catch (final RuntimeException exception) {
                    LOGGER.error("Failed to land {} moved by a piston at {} in {}", block.moved.key(), block.position, world.key(), exception);
                }
                return;
            }
            block.progress = Math.min(1.0f, block.progress + PROGRESS_PER_TICK);
            try {
                pushEntities(world, block);
            } catch (final RuntimeException exception) {
                LOGGER.error("Failed to push the entities in the way of a piston at {} in {}", block.position, world.key(), exception);
            }
            save(world, block);
        }

        boolean isEmpty() {
            return blocks.isEmpty();
        }

        void clear() {
            blocks.values().forEach(block -> block.gone = true);
            blocks.clear();
            releaseTicket();
        }

        void releaseTicket() {
            final ChunkPos held = ticket;
            if (held != null) {
                ticket = null;
                regionizer.removeTicket(key.dimension(), held);
            }
        }
    }
}
