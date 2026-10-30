package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.data.BannerPattern;
import net.kyori.adventure.key.KeyPattern;

/**
 * Keys of the vanilla tags of the {@code minecraft:banner_pattern} registry.
 */
public final class BannerPatternTagKeys {
    /**
     * Tag {@code #minecraft:no_item_required}.
     */
    public static final TagKey<BannerPattern> NO_ITEM_REQUIRED = create("no_item_required");

    /**
     * Tag {@code #minecraft:pattern_item/bordure_indented}.
     */
    public static final TagKey<BannerPattern> PATTERN_ITEM_BORDURE_INDENTED = create("pattern_item/bordure_indented");

    /**
     * Tag {@code #minecraft:pattern_item/creeper}.
     */
    public static final TagKey<BannerPattern> PATTERN_ITEM_CREEPER = create("pattern_item/creeper");

    /**
     * Tag {@code #minecraft:pattern_item/field_masoned}.
     */
    public static final TagKey<BannerPattern> PATTERN_ITEM_FIELD_MASONED = create("pattern_item/field_masoned");

    /**
     * Tag {@code #minecraft:pattern_item/flow}.
     */
    public static final TagKey<BannerPattern> PATTERN_ITEM_FLOW = create("pattern_item/flow");

    /**
     * Tag {@code #minecraft:pattern_item/flower}.
     */
    public static final TagKey<BannerPattern> PATTERN_ITEM_FLOWER = create("pattern_item/flower");

    /**
     * Tag {@code #minecraft:pattern_item/globe}.
     */
    public static final TagKey<BannerPattern> PATTERN_ITEM_GLOBE = create("pattern_item/globe");

    /**
     * Tag {@code #minecraft:pattern_item/guster}.
     */
    public static final TagKey<BannerPattern> PATTERN_ITEM_GUSTER = create("pattern_item/guster");

    /**
     * Tag {@code #minecraft:pattern_item/mojang}.
     */
    public static final TagKey<BannerPattern> PATTERN_ITEM_MOJANG = create("pattern_item/mojang");

    /**
     * Tag {@code #minecraft:pattern_item/piglin}.
     */
    public static final TagKey<BannerPattern> PATTERN_ITEM_PIGLIN = create("pattern_item/piglin");

    /**
     * Tag {@code #minecraft:pattern_item/skull}.
     */
    public static final TagKey<BannerPattern> PATTERN_ITEM_SKULL = create("pattern_item/skull");

    private BannerPatternTagKeys() {
        throw new UnsupportedOperationException("BannerPatternTagKeys cannot be instantiated.");
    }

    private static TagKey<BannerPattern> create(@KeyPattern final String value) {
        return TagKey.create(RegistryKey.BANNER_PATTERN, value);
    }
}
