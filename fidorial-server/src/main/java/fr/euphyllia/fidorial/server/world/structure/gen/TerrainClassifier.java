package fr.euphyllia.fidorial.server.world.structure.gen;

import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TerrainClassifier {

    public static final byte IGNORED = 0;
    public static final byte FLUID = 1;
    public static final byte GROUND = 2;

    private static final Map<BlockState, Byte> CACHE = new ConcurrentHashMap<>();

    private TerrainClassifier() {
    }

    public static byte classify(final BlockState state) {
        if (state.isAir()) {
            return IGNORED;
        }
        final Byte cached = CACHE.get(state);
        if (cached != null) {
            return cached;
        }
        final byte computed = compute(state);
        CACHE.put(state, computed);
        return computed;
    }

    public static boolean isGround(final BlockState state) {
        return classify(state) == GROUND;
    }

    private static byte compute(final BlockState state) {
        final String name = state.name().value();
        if (name.equals("water") || name.equals("lava") || name.equals("bubble_column")
                || name.contains("kelp") || name.contains("seagrass") || name.equals("sea_pickle")
                || name.contains("coral") || name.equals("ice") || name.equals("packed_ice")
                || name.equals("blue_ice") || name.equals("frosted_ice")) {
            return FLUID;
        }
        if (name.equals("dirt_path") || name.equals("farmland") || name.equals("mud") || name.equals("soul_sand")) {
            return GROUND;
        }
        if (name.endsWith("_log") || name.endsWith("_wood") || name.endsWith("_stem") || name.endsWith("_hyphae")
                || name.endsWith("mushroom_block") || name.equals("pumpkin") || name.equals("melon")
                || name.equals("bee_nest") || name.equals("carved_pumpkin") || name.equals("jack_o_lantern")
                || name.equals("mangrove_roots") || name.equals("muddy_mangrove_roots")) {
            return IGNORED;
        }
        if (BlockStateLightProperties.opacity(state.name()) < 15) {
            return "true".equals(state.properties().get("waterlogged")) ? FLUID : IGNORED;
        }
        return GROUND;
    }
}
