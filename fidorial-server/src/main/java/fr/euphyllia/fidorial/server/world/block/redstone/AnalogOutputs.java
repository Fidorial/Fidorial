package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.Registry;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockProperties;
import fr.fidorial.world.block.BlockProperty;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.SignalGetter;

import java.util.HashMap;
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

    private AnalogOutputs() {
        throw new UnsupportedOperationException("AnalogOutputs cannot be instantiated.");
    }

    /**
     * {@return the redstone behaviour of every vanilla block whose state alone gives a comparator a
     * level, by block}
     *
     * @param blocks the block registry, whose tags give the candle cakes and copper golem statues
     */
    public static Map<TypedKey<BlockType>, RedstoneBehaviour> vanilla(final Registry<BlockType> blocks) {
        final Map<TypedKey<BlockType>, RedstoneBehaviour> outputs = new HashMap<>();
        // a slice is worth 2: 14 for a whole cake, 2 for its last slice
        outputs.put(BlockTypeKeys.CAKE, new StateOutput(state -> (7 - number(state, "bites")) * 2));
        final StateOutput wholeCake = new StateOutput(_ -> 14);
        for (final TypedKey<BlockType> candleCake : blocks.tag(BlockTypeTagKeys.CANDLE_CAKES)) {
            outputs.put(candleCake, wholeCake);
        }

        final StateOutput level = new StateOutput(state -> number(state, "level"));
        outputs.put(BlockTypeKeys.COMPOSTER, level);
        outputs.put(BlockTypeKeys.WATER_CAULDRON, level);
        outputs.put(BlockTypeKeys.POWDER_SNOW_CAULDRON, level);
        outputs.put(BlockTypeKeys.LAVA_CAULDRON, new StateOutput(_ -> 3));
        outputs.put(BlockTypeKeys.CAULDRON, new StateOutput(_ -> 0));

        outputs.put(BlockTypeKeys.END_PORTAL_FRAME, new StateOutput(state -> state.get(BlockProperties.EYE) ? 15 : 0));
        // vanilla scales the four charges to 0, 3, 7, 11 and 15
        outputs.put(BlockTypeKeys.RESPAWN_ANCHOR, new StateOutput(state -> (int) Math.floor(number(state, "charges") / 4.0f * 15)));

        final StateOutput honey = new StateOutput(state -> number(state, "honey_level"));
        outputs.put(BlockTypeKeys.BEEHIVE, honey);
        outputs.put(BlockTypeKeys.BEE_NEST, honey);

        // 1 standing, 2 sitting, 3 running, 4 star
        final StateOutput pose = new StateOutput(state -> state.get(BlockProperties.COPPER_GOLEM_POSE).ordinal() + 1);
        for (final TypedKey<BlockType> statue : blocks.tag(BlockTypeTagKeys.COPPER_GOLEM_STATUES)) {
            outputs.put(statue, pose);
        }
        return Map.copyOf(outputs);
    }

    private static int number(final BlockData state, final String property) {
        return state.type().property(property) instanceof final BlockProperty.Range range ? state.get(range) : 0;
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
