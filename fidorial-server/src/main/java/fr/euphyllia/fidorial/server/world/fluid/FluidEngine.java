package fr.euphyllia.fidorial.server.world.fluid;

import fr.euphyllia.fidorial.server.debug.DebugGameEvents;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundBlockUpdatePacket;
import fr.euphyllia.fidorial.server.schedulers.RegionWideTickHandler;
import fr.euphyllia.fidorial.server.world.BlockEditService;
import fr.euphyllia.fidorial.server.world.BlockStateRegistry;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.ServerWorldManager;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Position;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameEvent;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.fluid.FluidManager;
import fr.fidorial.world.fluid.FluidState;
import fr.fidorial.world.fluid.FluidType;
import fr.fidorial.world.redstone.TickPriority;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public final class FluidEngine implements FluidManager {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FluidEngine.class);

    /**
     * What a fluid sees where the chunk is not loaded: a wall.
     */
    private static final BlockState UNLOADED = BlockState.withoutProperties(BlockTypeKeys.BARRIER);

    private static final BlockFace[] HORIZONTAL = {
            BlockFace.NORTH, BlockFace.SOUTH,
            BlockFace.WEST, BlockFace.EAST
    };

    private final ServerWorldManager worlds;
    private final BlockStateRegistry blockRegistry;

    private volatile LightHook lightHook = (_, _, _, _) -> {};
    private volatile BlockEditService.RedstoneNotifier redstoneHook = (_, _, _, _) -> {};
    private volatile @Nullable DebugGameEvents debugEvents;

    public FluidEngine(
            final ServerWorldManager worlds,
            final BlockStateRegistry blockRegistry
    ) {
        this.worlds = worlds;
        this.blockRegistry = blockRegistry;
    }

    @Override
    public FluidState fluidAt(final Key world, final int x, final int y, final int z) {
        final ServerWorld w = worldByKey(world);
        if (w == null) {
            return FluidState.empty();
        }
        try {
            return FluidBlockCodec.fromBlock(read(w, x, y, z));
        } catch (final IOException e) {
            LOGGER.error("Fluid reading impossible at {},{},{}", x, y, z, e);
            return FluidState.empty();
        }
    }

    @Override
    public boolean placeSource(final Key world, final int x, final int y, final int z, final FluidType type) {
        final ServerWorld w = worldByKey(world);
        if (w == null) {
            return false;
        }
        final boolean applied = setAndBroadcast(w, x, y, z, FluidBlockCodec.toBlock(FluidState.source(type)));
        if (applied) {
            schedule(world, x, y, z, type);
            emitEvent(w, GameEventKeys.FLUID_PLACE, x, y, z);
        }
        return applied;
    }

    @Override
    public boolean removeFluid(final Key world, final int x, final int y, final int z) {
        final ServerWorld w = worldByKey(world);
        if (w == null) {
            return false;
        }
        if (fluidAt(world, x, y, z).isEmpty()) {
            return false;
        }
        final boolean applied = setAndBroadcast(w, x, y, z, BlockState.withoutProperties(BlockTypeKeys.AIR));
        if (applied) {
            notifyBlockChanged(world, x, y, z);
            emitEvent(w, GameEventKeys.FLUID_PICKUP, x, y, z);
        }
        return applied;
    }

    @Override
    public void scheduleUpdate(final Key world, final int x, final int y, final int z) {
        final FluidState state = fluidAt(world, x, y, z);
        if (state.type() != null) {
            schedule(world, x, y, z, state.type());
        }
    }

    @Override
    public void notifyBlockChanged(final Key world, final int x, final int y, final int z) {
        scheduleUpdate(world, x, y, z);
        for (final BlockFace dir : BlockFace.values()) {
            scheduleUpdate(world, x + dir.dx(), y + dir.dy(), z + dir.dz());
        }
    }

    private void schedule(final Key world, final int x, final int y, final int z, final FluidType type) {
        final ServerWorld w = worldByKey(world);
        if (w != null) {
            w.fluidTicks().schedule(Position.block(x, y, z), type, type.tickDelay(), TickPriority.NORMAL);
        }
    }

    /**
     * {@return the handler running the fluid ticks, to register on the regionizer after the block ticks}:
     * fluid ticks are saved with their chunk and only run in the chunks whose blocks tick
     */
    public RegionWideTickHandler tickHandler() {
        return (worldName, sectionKeys) -> {
            final ServerWorld world = worlds.world(worldName);
            if (world != null) {
                world.fluidTicks().tickRegion(sectionKeys, (position, _) -> runTick(worldName, position));
            }
        };
    }

    private void runTick(final Key world, final BlockPosition position) {
        try {
            tick(world, position.blockX(), position.blockY(), position.blockZ());
        } catch (final IOException e) {
            LOGGER.error("Fluid tick impossible at {}", position, e);
        }
    }

    /**
     * {@return the block at a position, or a barrier where its chunk is not loaded}: fluids never load
     * a chunk, and flow against one that is not loaded as against a wall
     */
    private static BlockState read(final ServerWorld world, final int x, final int y, final int z) throws IOException {
        return world.loadedColumn(x >> 4, z >> 4) == null ? UNLOADED : world.getBlock(x, y, z);
    }

    private void tick(final Key worldName, final int x, final int y, final int z) throws IOException {
        final ServerWorld world = worldByKey(worldName);
        if (world == null) {
            return;
        }

        FluidState self = FluidBlockCodec.fromBlock(read(world, x, y, z));
        if (self.isEmpty()) {
            return;
        }
        final FluidType type = self.type();

        // 1) Interactions lave <-> eau : la lave touchée par de l'eau se fige.
        if (type == FluidType.LAVA && touches(world, x, y, z, FluidType.WATER)) {
            final BlockState solidified = self.isSource() ? BlockState.withoutProperties(BlockTypeKeys.OBSIDIAN) : BlockState.withoutProperties(BlockTypeKeys.COBBLESTONE);
            if (setAndBroadcast(world, x, y, z, solidified)) {
                notifyBlockChanged(worldName, x, y, z);
            }
            return;
        }

        // 2) Recalcul du niveau pour les fluides en écoulement.
        if (!self.isSource()) {
            final FluidState recomputed = recomputeLevel(world, worldName, x, y, z, self);
            if (recomputed == null) {
                return; // le bloc s'est asséché
            }
            self = recomputed;
        }

        // 3) Écoulement vertical prioritaire, sinon étalement horizontal.
        if (type != null && !flowDown(world, worldName, x, y, z, type)) {
            spreadHorizontally(world, worldName, x, y, z, self);
        }
    }

    private @Nullable FluidState recomputeLevel(
            final ServerWorld world,
            final Key worldName,
            final int x,
            final int y,
            final int z,
            final FluidState self
    ) throws IOException {
        final FluidType type = self.type();
        if (type == null) {
            return null;
        }
        final FluidState above = FluidBlockCodec.fromBlock(read(world, x, y + 1, z));
        final boolean fedFromAbove = above.type() == type;

        int best = Integer.MAX_VALUE;
        int adjacentSources = 0;
        for (final BlockFace dir : HORIZONTAL) {
            final FluidState n = FluidBlockCodec.fromBlock(read(world, x + dir.dx(), y, z + dir.dz()));
            if (n.type() == type) {
                best = Math.min(best, n.effectiveLevel() + type.dropOff());
                if (n.isSource()) {
                    adjacentSources++;
                }
            }
        }

        // Source infinie : deux sources voisines + support en dessous.
        if (canFormSources(world, type) && adjacentSources >= 2) {
            final BlockState belowBlock = read(world, x, y - 1, z);
            final FluidState belowFluid = FluidBlockCodec.fromBlock(belowBlock);
            final boolean supported = (!belowBlock.isAir() && belowFluid.isEmpty()) || belowFluid.isSource();
            if (supported) {
                return applyIfChanged(world, worldName, x, y, z, self, FluidState.source(type));
            }
        }

        final FluidState wanted;
        if (fedFromAbove) {
            wanted = FluidState.fallingFluid(type);
        } else if (best <= type.maxSpreadLevel()) {
            wanted = FluidState.flowing(type, best);
        } else {
            // Plus alimenté : assèchement.
            if (setAndBroadcast(world, x, y, z, BlockState.withoutProperties(BlockTypeKeys.AIR))) {
                notifyBlockChanged(worldName, x, y, z);
            }
            return null;
        }
        return applyIfChanged(world, worldName, x, y, z, self, wanted);
    }

    private static boolean canFormSources(final ServerWorld world, final FluidType type) {
        return switch (type) {
            case WATER -> world.gameRuleValues().getBoolean(GameRuleKeys.WATER_SOURCE_CONVERSION);
            case LAVA -> world.gameRuleValues().getBoolean(GameRuleKeys.LAVA_SOURCE_CONVERSION);
        };
    }

    private FluidState applyIfChanged(
            final ServerWorld world, final Key worldName, final int x, final int y, final int z, final FluidState current, final FluidState wanted) {
        if (wanted.equals(current)) {
            return current;
        }
        if (setAndBroadcast(world, x, y, z, FluidBlockCodec.toBlock(wanted))) {
            notifyBlockChanged(worldName, x, y, z);
        }
        return wanted;
    }

    private boolean flowDown(final ServerWorld world, final Key worldName, final int x, final int y, final int z, final FluidType type)
            throws IOException {
        final BlockState belowBlock = read(world, x, y - 1, z);
        final FluidState below = FluidBlockCodec.fromBlock(belowBlock);

        // La lave qui tombe dans l'eau se fige en pierre taillée.
        if (type == FluidType.LAVA && below.type() == FluidType.WATER) {
            if (setAndBroadcast(world, x, y - 1, z, BlockState.withoutProperties(BlockTypeKeys.COBBLESTONE))) {
                notifyBlockChanged(worldName, x, y - 1, z);
            }
            return false;
        }

        if (below.type() == type) {
            if (below.isSource() || below.falling()) {
                return true; // la colonne coule déjà
            }
            // Un écoulement horizontal en dessous est remplacé par une chute.
            if (setAndBroadcast(world, x, y - 1, z, FluidBlockCodec.toBlock(FluidState.fallingFluid(type)))) {
                schedule(worldName, x, y - 1, z, type);
            }
            return true;
        }

        if (belowBlock.isAir()) {
            if (setAndBroadcast(world, x, y - 1, z, FluidBlockCodec.toBlock(FluidState.fallingFluid(type)))) {
                schedule(worldName, x, y - 1, z, type);
            }
            return true;
        }
        if (below.type() != null) {
            schedule(worldName, x, y - 1, z, below.type());
        }
        return false; // bloqué : étalement horizontal
    }

    private void spreadHorizontally(final ServerWorld world, final Key worldName, final int x, final int y, final int z, final FluidState self) {
        final FluidType type = self.type();
        if (type == null) {
            return;
        }
        final int spreadLevel = self.effectiveLevel() + type.dropOff();
        if (spreadLevel > type.maxSpreadLevel()) {
            return;
        }
        for (final BlockFace dir : HORIZONTAL) {
            spreadTo(world, worldName, x + dir.dx(), y, z + dir.dz(), type, spreadLevel);
        }
    }

    private void spreadTo(final ServerWorld world, final Key worldName, final int tx, final int y, final int tz, final FluidType type, final int spreadLevel) {
        // regions own a margin around their loaded chunks: another region only owns the neighbour
        // where regions never merge, and the fluid stops at the border, as at an unloaded chunk
        if (world.scheduler().isOwnedByCurrentThread(worldName, ChunkPos.fromBlock(tx, tz))) {
            spreadToOwned(world, worldName, tx, y, tz, type, spreadLevel);
        }
    }

    private void spreadToOwned(final ServerWorld world, final Key worldName, final int tx, final int y, final int tz, final FluidType type, final int spreadLevel) {
        try {
            final BlockState targetBlock = read(world, tx, y, tz);
            final FluidState target = FluidBlockCodec.fromBlock(targetBlock);

            final boolean canFlow = targetBlock.isAir()
                    || (target.type() == type && !target.isSource() && target.effectiveLevel() > spreadLevel);
            if (canFlow) {
                if (setAndBroadcast(world, tx, y, tz, FluidBlockCodec.toBlock(FluidState.flowing(type, spreadLevel)))) {
                    schedule(worldName, tx, y, tz, type);
                }
            } else if (target.type() != null && target.type() != type) {
                schedule(worldName, tx, y, tz, target.type());
            }
        } catch (final IOException e) {
            LOGGER.error("Fluid spread impossible at {},{},{}", tx, y, tz, e);
        }
    }

    private boolean touches(final ServerWorld world, final int x, final int y, final int z, final FluidType other) throws IOException {
        if (FluidBlockCodec.fromBlock(read(world, x, y + 1, z)).type() == other) {
            return true;
        }
        for (final BlockFace dir : HORIZONTAL) {
            if (FluidBlockCodec.fromBlock(read(world, x + dir.dx(), y, z + dir.dz()))
                    .type()
                    == other) {
                return true;
            }
        }
        return false;
    }

    private boolean setAndBroadcast(final ServerWorld world, final int x, final int y, final int z, final BlockState state) {
        if (world.loadedColumn(x >> 4, z >> 4) == null) {
            return false; // fluids never load a chunk
        }
        final BlockState previous;
        try {
            previous = world.getBlock(x, y, z);
            if (!world.setBlock(x, y, z, state)) {
                return false;
            }
        } catch (final IOException e) {
            LOGGER.error("Fluid writing impossible in {},{},{}", x, y, z, e);
            return false;
        }
        world.sendToChunkViewers(x >> 4, z >> 4,
                new ClientboundBlockUpdatePacket(Position.block(x, y, z), blockRegistry.networkId(state)));
        lightHook.onBlockChanged(world.dimension().id(), x, y, z);
        if (!previous.equals(state)) {
            redstoneHook.blockChanged(world, Position.block(x, y, z), previous, state);
        }
        return true;
    }

    private @Nullable ServerWorld worldByKey(@Nullable final Key key) {
        return key == null ? worlds.defaultWorld().orElse(null) : worlds.world(key);
    }

    public void setLightHook(final LightHook hook) {
        this.lightHook = hook;
    }

    /**
     * Sets who follows up on the blocks fluids change for redstone, as for the blocks changed
     * through the {@link BlockEditService}: water reaching an observer's face triggers it.
     *
     * @param hook told of every block a fluid changes
     */
    public void setRedstoneHook(final BlockEditService.RedstoneNotifier hook) {
        this.redstoneHook = hook;
    }

    public void setDebugEvents(final DebugGameEvents debugEvents) {
        this.debugEvents = debugEvents;
    }

    private void emitEvent(final ServerWorld world, final TypedKey<GameEvent> event, final int x, final int y, final int z) {
        final DebugGameEvents events = debugEvents;
        if (events != null) {
            events.emit(world, event, Position.block(x, y, z));
        }
    }

    @FunctionalInterface
    public interface LightHook {
        void onBlockChanged(Key world, int x, int y, int z);
    }
}
