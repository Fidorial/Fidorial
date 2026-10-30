package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.data.PaintingVariant;
import net.kyori.adventure.key.KeyPattern;

/**
 * Keys of the vanilla tags of the {@code minecraft:painting_variant} registry.
 */
public final class PaintingVariantTagKeys {
    /**
     * Tag {@code #minecraft:placeable}.
     */
    public static final TagKey<PaintingVariant> PLACEABLE = create("placeable");

    private PaintingVariantTagKeys() {
        throw new UnsupportedOperationException("PaintingVariantTagKeys cannot be instantiated.");
    }

    private static TagKey<PaintingVariant> create(@KeyPattern final String value) {
        return TagKey.create(RegistryKey.PAINTING_VARIANT, value);
    }
}
