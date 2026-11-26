package fr.euphyllia.fidorial.server.world.generation.feature;

import fr.euphyllia.fidorial.server.world.block.BlockCollision;
import fr.euphyllia.fidorial.server.world.block.BlockSolidity;
import fr.euphyllia.fidorial.server.world.block.BlockSupport;
import fr.euphyllia.fidorial.server.world.block.CollisionShape;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.BlockStates;
import fr.euphyllia.fidorial.server.world.chunk.Heightmap;
import fr.euphyllia.fidorial.server.world.fluid.FluidBlockCodec;
import fr.euphyllia.fidorial.server.world.fluid.FluidStateImpl;
import fr.euphyllia.fidorial.server.world.fluid.FluidType;
import fr.euphyllia.fidorial.server.world.generation.block.BlockLookup;
import fr.euphyllia.fidorial.server.world.generation.block.StatePalette;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Fluid;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.registry.keys.FluidKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.data.Bisected;
import fr.fidorial.world.redstone.SupportType;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntPredicate;

/**
 * What the decorations ask about the block states of a {@link StatePalette}, cached per state number. Nothing is
 * decided here: the fluids come from the fluid model of the server, the categories from the block tags, the
 * shapes from the collision and support data of the blocks.
 *
 * <p>Thread-safe: the flags of a state are worked out by whoever first asks, the result is always the same.</p>
 */
public final class StateFlags {

    public static final long AIR = 1L;
    public static final long WATER = 1L << 1;
    public static final long WATER_SOURCE = 1L << 2;
    public static final long LAVA = 1L << 3;
    public static final long LAVA_SOURCE = 1L << 4;
    /**
     * The block is a fluid itself, not a block holding one.
     */
    public static final long LIQUID_BLOCK = 1L << 5;
    public static final long REPLACEABLE = 1L << 6;
    /**
     * The block is solid, as {@link BlockSolidity} tells.
     */
    public static final long SOLID = 1L << 7;
    public static final long MOTION = 1L << 8;
    public static final long MOTION_NO_LEAVES = 1L << 9;
    public static final long LEAVES = 1L << 10;
    public static final long PROTECTED = 1L << 11;
    public static final long LOG = 1L << 12;
    /**
     * Something stands on the top of the collision shape, however small.
     */
    public static final long TOP_SURFACE = 1L << 13;
    public static final long FULL_COLLISION = 1L << 14;
    /**
     * A plant two blocks tall, set as a lower and an upper half.
     */
    public static final long TWO_BLOCKS_TALL = 1L << 15;
    private static final int FULL_FACES = 16;
    private static final int CENTER_FACES = 22;
    private static final long KNOWN = 1L << 62;

    private static final Set<String> TWO_HALVES = Set.of(Bisected.Half.UPPER.value(), Bisected.Half.LOWER.value());
    private static final BlockFace[] FACES = {BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};

    private final StatePalette palette;
    private final BlockLookup blocks;
    private final Object growLock = new Object();
    private volatile long[] flags = new long[256];
    private final Map<Edit, Character> edits = new ConcurrentHashMap<>();

    private record Edit(char id, String property, String value) {
    }

    public StateFlags(final StatePalette palette, final BlockLookup blocks) {
        this.palette = palette;
        this.blocks = blocks;
    }

    public StatePalette palette() {
        return palette;
    }

    public BlockLookup blocks() {
        return blocks;
    }

    /**
     * {@return every flag of a state}
     */
    public long flags(final char id) {
        final long[] known = flags;
        if (id < known.length) {
            final long value = known[id];
            if (value != 0L) {
                return value;
            }
        }
        final long value = compute(palette.state(id)) | KNOWN;
        synchronized (growLock) {
            long[] current = flags;
            if (id >= current.length) {
                current = Arrays.copyOf(current, Math.max(current.length * 2, id + 1));
            }
            current[id] = value;
            flags = current;
        }
        return value;
    }

    public boolean has(final char id, final long flag) {
        return (flags(id) & flag) != 0L;
    }

    public boolean isAir(final char id) {
        return has(id, AIR);
    }

    public boolean hasFluid(final char id) {
        return has(id, WATER | LAVA);
    }

    /// Whether a state counts as the top of its column for a heightmap, as [Heightmap#occupies()] tells.
    public boolean counts(final Heightmap heightmap, final char id) {
        return switch (heightmap) {
            case WORLD_SURFACE_WG, WORLD_SURFACE -> !isAir(id);
            case OCEAN_FLOOR_WG, OCEAN_FLOOR -> has(id, MOTION);
            case MOTION_BLOCKING -> has(id, MOTION) || hasFluid(id);
            case MOTION_BLOCKING_NO_LEAVES -> has(id, MOTION_NO_LEAVES) || hasFluid(id);
        };
    }

    /**
     * {@return the fluid a state holds: water, lava, or none}
     */
    public TypedKey<Fluid> fluid(final char id) {
        final long value = flags(id);
        if ((value & WATER) != 0L) {
            return FluidKeys.WATER;
        }
        return (value & LAVA) != 0L ? FluidKeys.LAVA : FluidKeys.EMPTY;
    }

    /**
     * {@return {@code true} for a block of water itself, still or flowing, not a block holding water}
     */
    public boolean isWaterBlock(final char id) {
        return (flags(id) & (LIQUID_BLOCK | WATER)) == (LIQUID_BLOCK | WATER);
    }

    /**
     * {@return {@code true} for a block of lava itself, still or flowing}
     */
    public boolean isLavaBlock(final char id) {
        return (flags(id) & (LIQUID_BLOCK | LAVA)) == (LIQUID_BLOCK | LAVA);
    }

    /**
     * {@return {@code true} if a face of the state can hold something across its whole surface}
     */
    public boolean sturdy(final char id, final BlockFace face) {
        return has(id, 1L << (FULL_FACES + faceIndex(face)));
    }

    /**
     * {@return {@code true} if a face of the state can hold something by its middle}
     */
    public boolean sturdyCenter(final char id, final BlockFace face) {
        return has(id, 1L << (CENTER_FACES + faceIndex(face)));
    }

    private static int faceIndex(final BlockFace face) {
        return switch (face) {
            case DOWN -> 0;
            case UP -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
        };
    }

    private long compute(final BlockState state) {
        long value = 0L;
        if (state.isAir()) {
            return AIR;
        }
        final FluidStateImpl fluid = FluidBlockCodec.fromBlock(state);
        if (fluid.type() == FluidType.WATER) {
            value |= WATER | (fluid.isSource() ? WATER_SOURCE : 0L);
        } else if (fluid.type() == FluidType.LAVA) {
            value |= LAVA | (fluid.isSource() ? LAVA_SOURCE : 0L);
        }
        if (FluidType.byBlockKey(state.name()) != null) {
            value |= LIQUID_BLOCK;
        }
        if (blocks.is(state, BlockTypeTagKeys.REPLACEABLE)) {
            value |= REPLACEABLE;
        }
        if (blocks.is(state, BlockTypeTagKeys.BLOCKS_MOTION_IN_HEIGHTMAP)) {
            value |= MOTION;
        }
        if (blocks.is(state, BlockTypeTagKeys.BLOCKS_MOTION_IN_HEIGHTMAP_NO_LEAVES)) {
            value |= MOTION_NO_LEAVES;
        }
        if (blocks.is(state, BlockTypeTagKeys.LEAVES)) {
            value |= LEAVES;
        }
        if (blocks.is(state, BlockTypeTagKeys.LOGS)) {
            value |= LOG;
        }
        if (blocks.is(state, BlockTypeTagKeys.FEATURES_CANNOT_REPLACE)) {
            value |= PROTECTED;
        }
        if (isTwoBlocksTall(state)) {
            value |= TWO_BLOCKS_TALL;
        }
        if (BlockSolidity.isSolid(state, tag -> blocks.is(state, tag))) {
            value |= SOLID;
        }
        final CollisionShape shape = BlockCollision.shape(state);
        if (shape.isFullBlock()) {
            value |= FULL_COLLISION;
        }
        for (int box = 0; box < shape.boxCount(); box++) {
            if (shape.maxY(box) >= 1.0) {
                value |= TOP_SURFACE;
                break;
            }
        }
        final BlockData data = data(state);
        for (int face = 0; face < FACES.length; face++) {
            if (BlockSupport.isFaceSturdy(data, FACES[face], SupportType.FULL)) {
                value |= 1L << (FULL_FACES + face);
            }
            if (BlockSupport.isFaceSturdy(data, FACES[face], SupportType.CENTER)) {
                value |= 1L << (CENTER_FACES + face);
            }
        }
        return value;
    }

    /**
     * {@return {@code true} for a plant two blocks tall}: every state of the block is an upper or a lower half, as
     * for doors, which are no plants
     */
    private boolean isTwoBlocksTall(final BlockState state) {
        if (!state.properties().containsKey("half") || blocks.is(state, BlockTypeTagKeys.DOORS)) {
            return false;
        }
        final BlockState[] all = BlockStates.statesOf(state.name());
        if (all == null) {
            return false;
        }
        for (final BlockState candidate : all) {
            if (!TWO_HALVES.contains(candidate.properties().get("half"))) {
                return false;
            }
        }
        return true;
    }

    private static @Nullable BlockData data(final BlockState state) {
        final BlockData cached = state.data();
        if (cached != null) {
            return cached;
        }
        try {
            return Blocks.registry().type(state.name()).map(type -> type.dataOrNull(state.properties())).orElse(null);
        } catch (final IllegalStateException _) {
            return null;
        }
    }

    /**
     * {@return the number of a state with one property changed; the same state when it has no such property}
     */
    public char with(final char id, final String property, final String value) {
        final Edit edit = new Edit(id, property, value);
        final Character known = edits.get(edit);
        if (known != null) {
            return known;
        }
        final BlockState state = palette.state(id);
        final String current = state.properties().get(property);
        final char result;
        if (current == null || current.equals(value)) {
            result = id;
        } else {
            final Map<String, String> values = new HashMap<>(state.properties());
            values.put(property, value);
            result = palette.id(BlockState.withProperties(state.name(), values));
        }
        edits.put(edit, result);
        return result;
    }

    /**
     * {@return the number of a state with one property changed, when the block has the property and allows the
     * value; the same state otherwise}
     */
    public char withIfAllowed(final char id, final String property, final String value) {
        final BlockState state = palette.state(id);
        if (!state.properties().containsKey(property)) {
            return id;
        }
        final Boolean allowed = allowedValues.computeIfAbsent(state.name().key().asString() + '|' + property + '=' + value, key -> {
            final BlockState[] all = BlockStates.statesOf(state.name());
            if (all == null) {
                return false;
            }
            for (final BlockState candidate : all) {
                if (value.equals(candidate.properties().get(property))) {
                    return true;
                }
            }
            return false;
        });
        return allowed ? with(id, property, value) : id;
    }

    private final Map<String, Boolean> allowedValues = new ConcurrentHashMap<>();

    /**
     * {@return the value of a property of a state, {@code null} when it has none}
     */
    public @Nullable String property(final char id, final String property) {
        return palette.state(id).properties().get(property);
    }

    /**
     * {@return {@code true} if a state is of the given block}
     */
    public boolean is(final char id, final TypedKey<BlockType> block) {
        return palette.state(id).name().equals(block);
    }

    /**
     * {@return {@code true} if a state belongs to a block tag}
     */
    public boolean isTagged(final char id, final TagKey<BlockType> tag) {
        return membership(tag).test(id);
    }

    /**
     * {@return a test of the states of a block tag, which remembers its answers}
     */
    public Membership membership(final TagKey<BlockType> tag) {
        return memberships.computeIfAbsent(tag, key -> new Membership(id -> blocks.is(palette.state((char) id), key)));
    }

    /**
     * {@return a test of the states of some blocks, which remembers its answers}
     */
    public Membership membership(final Set<TypedKey<BlockType>> members) {
        return new Membership(id -> members.contains(palette.state((char) id).name()));
    }

    private final Map<TagKey<BlockType>, Membership> memberships = new ConcurrentHashMap<>();

    /**
     * Whether states belong to a set, remembered per state number.
     */
    public static final class Membership {

        private final IntPredicate source;
        private volatile byte[] answers = new byte[256];

        Membership(final IntPredicate source) {
            this.source = source;
        }

        public boolean test(final char id) {
            final byte[] known = answers;
            if (id < known.length && known[id] != 0) {
                return known[id] > 0;
            }
            final boolean member = source.test(id);
            synchronized (this) {
                byte[] current = answers;
                if (id >= current.length) {
                    current = Arrays.copyOf(current, Math.max(current.length * 2, id + 1));
                }
                current[id] = member ? (byte) 1 : (byte) -1;
                answers = current;
            }
            return member;
        }
    }
}
