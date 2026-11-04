package fr.euphyllia.fidorial.server.world.storage;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.DimensionTypeKeys;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

public record Dimension(
        Key id,
        @Nullable String legacyFolder,
        TypedKey<DimensionTypeDefinition> dimensionType
) {

    public static final Dimension OVERWORLD = new Dimension(Key.key("overworld"), null, DimensionTypeKeys.OVERWORLD);
    public static final Dimension THE_NETHER = new Dimension(Key.key("the_nether"), "DIM-1", DimensionTypeKeys.THE_NETHER);
    public static final Dimension THE_END = new Dimension(Key.key("the_end"), "DIM1", DimensionTypeKeys.THE_END);

    public static Dimension datapack(final Key id, final TypedKey<DimensionTypeDefinition> dimensionType) {
        return new Dimension(id, null, dimensionType);
    }

    public static Dimension datapack(final Key id) {
        return datapack(id, DimensionTypeKeys.OVERWORLD);
    }
}
