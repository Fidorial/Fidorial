package fr.euphyllia.fidorial.server.world.structure.worldgen;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.registry.keys.BlockTypeTagKeys;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

// https://minecraft.wiki/w/Heightmap
public enum Heightmap {
    WORLD_SURFACE_WG(0, false, true, state -> !state.isAir()),
    WORLD_SURFACE(1, true, false, state -> !state.isAir()),
    OCEAN_FLOOR_WG(2, false, true, state -> state.isTagged(BlockTypeTagKeys.BLOCKS_MOTION_IN_HEIGHTMAP)),
    OCEAN_FLOOR(3, false, false, state -> state.isTagged(BlockTypeTagKeys.BLOCKS_MOTION_IN_HEIGHTMAP)),
    MOTION_BLOCKING(4, true, false, state -> state.isFluid() || state.isTagged(BlockTypeTagKeys.BLOCKS_MOTION_IN_HEIGHTMAP)),
    MOTION_BLOCKING_NO_LEAVES(5, true, false, state -> state.isFluid() || state.isTagged(BlockTypeTagKeys.BLOCKS_MOTION_IN_HEIGHTMAP_NO_LEAVES));

    public static final List<Heightmap> SENT_TO_CLIENTS = Arrays.stream(values()).filter(Heightmap::sentToClients).toList();

    private final int networkId;
    private final boolean sentToClients;
    private final boolean generationOnly;
    private final Predicate<BlockState> occupies;

    Heightmap(final int networkId, final boolean sentToClients, final boolean generationOnly, final Predicate<BlockState> occupies) {
        this.networkId = networkId;
        this.sentToClients = sentToClients;
        this.generationOnly = generationOnly;
        this.occupies = occupies;
    }

    public int networkId() {
        return networkId;
    }

    public boolean sentToClients() {
        return sentToClients;
    }

    /// Whether this heightmap is dropped once the chunk is fully generated.
    public boolean generationOnly() {
        return generationOnly;
    }

    /// Whether a block counts as the top of its column.
    public Predicate<BlockState> occupies() {
        return occupies;
    }

    public boolean ignoresFluids() {
        return this == OCEAN_FLOOR_WG || this == OCEAN_FLOOR;
    }

    public static Heightmap byName(final String name, final Heightmap fallback) {
        try {
            return valueOf(name.strip().toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException _) {
            return fallback;
        }
    }
}
