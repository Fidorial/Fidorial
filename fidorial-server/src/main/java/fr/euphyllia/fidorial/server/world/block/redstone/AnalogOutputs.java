package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.SignalGetter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * The blocks a comparator reads a level out of their state alone, ports of the
 * {@code getAnalogOutputSignal} of vanilla's cake, candle cakes, composter, cauldrons, end portal
 * frame, respawn anchor, beehives and copper golem statues.
 *
 * <p>The levels vanilla reads out of block entities, such as how full a container is, are not
 * read yet.</p>
 */
public final class AnalogOutputs {

    private static final List<String> STATUE_POSES = List.of("standing", "sitting", "running", "star");

    private AnalogOutputs() {
        throw new UnsupportedOperationException("AnalogOutputs cannot be instantiated.");
    }

    /**
     * {@return the redstone behaviour of every vanilla block whose state alone gives a comparator a
     * level, by block}
     */
    public static Map<TypedKey<BlockType>, RedstoneBehaviour> vanilla() {
        final Map<TypedKey<BlockType>, RedstoneBehaviour> outputs = new HashMap<>();
        // a slice is worth 2: 14 for a whole cake, 2 for its last slice
        outputs.put(BlockTypeKeys.CAKE, new StateOutput(state -> (7 - number(state, "bites")) * 2));
        final StateOutput wholeCake = new StateOutput(_ -> 14);
        for (final TypedKey<BlockType> candleCake : List.of(BlockTypeKeys.CANDLE_CAKE, BlockTypeKeys.WHITE_CANDLE_CAKE,
                BlockTypeKeys.ORANGE_CANDLE_CAKE, BlockTypeKeys.MAGENTA_CANDLE_CAKE, BlockTypeKeys.LIGHT_BLUE_CANDLE_CAKE,
                BlockTypeKeys.YELLOW_CANDLE_CAKE, BlockTypeKeys.LIME_CANDLE_CAKE, BlockTypeKeys.PINK_CANDLE_CAKE,
                BlockTypeKeys.GRAY_CANDLE_CAKE, BlockTypeKeys.LIGHT_GRAY_CANDLE_CAKE, BlockTypeKeys.CYAN_CANDLE_CAKE,
                BlockTypeKeys.PURPLE_CANDLE_CAKE, BlockTypeKeys.BLUE_CANDLE_CAKE, BlockTypeKeys.BROWN_CANDLE_CAKE,
                BlockTypeKeys.GREEN_CANDLE_CAKE, BlockTypeKeys.RED_CANDLE_CAKE, BlockTypeKeys.BLACK_CANDLE_CAKE)) {
            outputs.put(candleCake, wholeCake);
        }

        final StateOutput level = new StateOutput(state -> number(state, "level"));
        outputs.put(BlockTypeKeys.COMPOSTER, level);
        outputs.put(BlockTypeKeys.WATER_CAULDRON, level);
        outputs.put(BlockTypeKeys.POWDER_SNOW_CAULDRON, level);
        outputs.put(BlockTypeKeys.LAVA_CAULDRON, new StateOutput(_ -> 3));
        outputs.put(BlockTypeKeys.CAULDRON, new StateOutput(_ -> 0));

        outputs.put(BlockTypeKeys.END_PORTAL_FRAME, new StateOutput(state -> "true".equals(state.get("eye")) ? 15 : 0));
        // vanilla scales the four charges to 0, 3, 7, 11 and 15
        outputs.put(BlockTypeKeys.RESPAWN_ANCHOR, new StateOutput(state -> (int) Math.floor(number(state, "charges") / 4.0f * 15)));

        final StateOutput honey = new StateOutput(state -> number(state, "honey_level"));
        outputs.put(BlockTypeKeys.BEEHIVE, honey);
        outputs.put(BlockTypeKeys.BEE_NEST, honey);

        // 1 standing, 2 sitting, 3 running, 4 star
        final StateOutput pose = new StateOutput(state -> STATUE_POSES.indexOf(state.get("copper_golem_pose")) + 1);
        for (final TypedKey<BlockType> statue : List.of(BlockTypeKeys.COPPER_GOLEM_STATUE,
                BlockTypeKeys.EXPOSED_COPPER_GOLEM_STATUE, BlockTypeKeys.WEATHERED_COPPER_GOLEM_STATUE,
                BlockTypeKeys.OXIDIZED_COPPER_GOLEM_STATUE, BlockTypeKeys.WAXED_COPPER_GOLEM_STATUE,
                BlockTypeKeys.WAXED_EXPOSED_COPPER_GOLEM_STATUE, BlockTypeKeys.WAXED_WEATHERED_COPPER_GOLEM_STATUE,
                BlockTypeKeys.WAXED_OXIDIZED_COPPER_GOLEM_STATUE)) {
            outputs.put(statue, pose);
        }
        return Map.copyOf(outputs);
    }

    private static int number(final BlockData state, final String property) {
        final String value = state.get(property);
        return value == null ? 0 : Integer.parseInt(value);
    }

    /**
     * A block whose state gives a comparator a level, and that does nothing else in redstone.
     */
    private record StateOutput(ToIntFunction<BlockData> level) implements RedstoneBehaviour {

        @Override
        public boolean hasAnalogOutput(final BlockData state) {
            return true;
        }

        @Override
        public int analogOutput(final SignalGetter world, final BlockPosition position, final BlockData state,
                                final BlockFace direction) {
            return level.applyAsInt(state);
        }
    }
}
