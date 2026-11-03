package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.BlockStateProvider;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:worldgen/block_state_provider} registry.
 */
public final class BlockStateProviderKeys {
    /**
     * Key for {@code minecraft:cave_vines_body}.
     */
    public static final TypedKey<BlockStateProvider> CAVE_VINES_BODY = create("cave_vines_body");

    /**
     * Key for {@code minecraft:cave_vines_head}.
     */
    public static final TypedKey<BlockStateProvider> CAVE_VINES_HEAD = create("cave_vines_head");

    /**
     * Key for {@code minecraft:flower_flower_forest}.
     */
    public static final TypedKey<BlockStateProvider> FLOWER_FLOWER_FOREST = create("flower_flower_forest");

    /**
     * Key for {@code minecraft:flower_meadow}.
     */
    public static final TypedKey<BlockStateProvider> FLOWER_MEADOW = create("flower_meadow");

    /**
     * Key for {@code minecraft:flower_plain}.
     */
    public static final TypedKey<BlockStateProvider> FLOWER_PLAIN = create("flower_plain");

    /**
     * Key for {@code minecraft:mangrove_propagule}.
     */
    public static final TypedKey<BlockStateProvider> MANGROVE_PROPAGULE = create("mangrove_propagule");

    /**
     * Key for {@code minecraft:podzol_beneath_tree}.
     */
    public static final TypedKey<BlockStateProvider> PODZOL_BENEATH_TREE = create("podzol_beneath_tree");

    /**
     * Key for {@code minecraft:soil_beneath_tree}.
     */
    public static final TypedKey<BlockStateProvider> SOIL_BENEATH_TREE = create("soil_beneath_tree");

    private static final List<TypedKey<BlockStateProvider>> VALUES = List.of(
        CAVE_VINES_BODY,
        CAVE_VINES_HEAD,
        FLOWER_FLOWER_FOREST,
        FLOWER_MEADOW,
        FLOWER_PLAIN,
        MANGROVE_PROPAGULE,
        PODZOL_BENEATH_TREE,
        SOIL_BENEATH_TREE
    );

    private BlockStateProviderKeys() {
        throw new UnsupportedOperationException("BlockStateProviderKeys cannot be instantiated.");
    }

    private static TypedKey<BlockStateProvider> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.BLOCK_STATE_PROVIDER, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<BlockStateProvider>> values() {
        return VALUES.stream();
    }
}
