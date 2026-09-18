package fr.euphyllia.fidorial.server.world.block;

import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.interaction.FidorialBlockInteractionContext;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockPos;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.Map;

public final class FarmlandBlock {

    public static final Key KEY = BlockTypeKeys.FARMLAND.key();
    public static final String MOISTURE = "moisture";
    public static final int MAX_MOISTURE = 7;
    public static final int WATER_RANGE = 4;

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FarmlandBlock.class);

    private FarmlandBlock() {
        throw new UnsupportedOperationException("FarmlandBlock cannot be instantiated.");
    }

    public static boolean is(final BlockState state) {
        return KEY.equals(state.name());
    }

    public static int moisture(final BlockState state) {
        final String raw = state.properties().get(MOISTURE);
        if (raw == null) {
            return 0;
        }
        try {
            return Integer.parseInt(raw);
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    public static BlockState withMoisture(final int moisture) {
        final int clamped = Math.clamp(moisture, 0, MAX_MOISTURE);
        return BlockState.of(KEY, Map.of(MOISTURE, Integer.toString(clamped)));
    }

    public static boolean isHydrated(final ServerWorld world, final BlockPos pos) {
        for (int dx = -WATER_RANGE; dx <= WATER_RANGE; dx++) {
            for (int dz = -WATER_RANGE; dz <= WATER_RANGE; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    final BlockState state = FidorialBlockInteractionContext.readState(
                            world, pos.offset(dx, dy, dz));
                    if (isWater(state)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isWater(final BlockState state) {
        return BlockTypeKeys.WATER.key().equals(state.name())
                || "true".equals(state.properties().get("waterlogged"));
    }
}
