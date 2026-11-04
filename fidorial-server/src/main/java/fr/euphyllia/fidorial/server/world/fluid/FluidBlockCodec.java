package fr.euphyllia.fidorial.server.world.fluid;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.fluid.FluidState;
import fr.fidorial.world.fluid.FluidType;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class FluidBlockCodec {

    private static final String LEVEL = "level";
    private static final int FALLING_OFFSET = 8;

    private FluidBlockCodec() {
    }

    public static FluidState fromBlock(@Nullable final BlockState block) {
        if (block == null) {
            return FluidState.empty();
        }
        final FluidType type = FluidType.byBlockKey(block.name());
        if (type == null) {
            return FluidState.empty();
        }
        final String raw = block.properties().get(LEVEL);
        int level = 0;
        if (raw != null) {
            try {
                level = Integer.parseInt(raw);
            } catch (final NumberFormatException ignored) {
            }
        }
        if (level >= FALLING_OFFSET) {
            return FluidState.fallingFluid(type);
        }
        return new FluidState(type, level, false);
    }

    public static BlockState toBlock(final FluidState state) {
        if (state.type() == null) {
            return BlockState.withoutProperties(BlockTypeKeys.AIR);
        }
        final int level = state.falling() ? FALLING_OFFSET : clamp(state.level());
        return BlockState.withProperties(state.type().blockKey(), Map.of(LEVEL, String.valueOf(level)));
    }

    private static int clamp(final int level) {
        return Math.clamp(level, 0, 7);
    }
}
