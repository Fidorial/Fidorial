package fr.euphyllia.fidorial.server.world.light;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.BlockState.LightProperties;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.block.Blocks;
import org.jspecify.annotations.Nullable;

public final class BlockLightProperties {

    public static final int OPAQUE = 15;

    private static final LightProperties AIR_PROPS = new LightProperties(0, 0);
    private static final LightProperties UNKNOWN_PROPS = new LightProperties(OPAQUE, 0);

    public static int opacity(final BlockState state) {
        return propsOf(state).opacity();
    }

    public static boolean occludes(final BlockState state) {
        return propsOf(state).opacity() >= OPAQUE;
    }

    public static int emission(final BlockState state) {
        return propsOf(state).emission();
    }

    private static LightProperties propsOf(final BlockState state) {
        final LightProperties cached = state.lightProperties();
        if (cached != null) {
            return cached;
        }
        if (state.isAir()) {
            state.setLightProperties(AIR_PROPS);
            return AIR_PROPS;
        }

        final BlockRegistry registry;
        try {
            registry = Blocks.registry();
        } catch (final IllegalStateException _) {
            // registry not bootstrapped yet
            return UNKNOWN_PROPS;
        }

        final Resolved resolved = resolve(registry, state);
        final LightProperties props = resolved == null
                ? UNKNOWN_PROPS
                : new LightProperties(
                resolved.behaviour().lightOpacity(resolved.data()),
                resolved.behaviour().lightEmission(resolved.data()));

        state.setLightProperties(props);
        return props;
    }

    private static @Nullable Resolved resolve(final BlockRegistry registry, final BlockState state) {
        final BlockBehaviour behaviour = registry.behaviour(state.name()).orElse(null);
        if (behaviour == null) {
            return null;
        }
        final BlockData cached = state.data();
        final BlockData data = cached != null ? cached : behaviour.type().dataOrNull(state.properties());
        return data == null ? null : new Resolved(behaviour, data);
    }

    public static boolean hasEmission(final BlockState state) {
        return emission(state) > 0;
    }

    private record Resolved(BlockBehaviour behaviour, BlockData data) {
    }
}
