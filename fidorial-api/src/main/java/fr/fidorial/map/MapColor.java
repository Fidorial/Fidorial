package fr.fidorial.map;

/**
 * The base colors of a map, as the client knows them.
 *
 * <p>Every block has one of these colors (see {@link MapManager#colorOf}); a map pixel is a base
 * color drawn in one of four {@linkplain MapShade shades}, see {@link MapPalette}. The constants are
 * declared in network order: {@link #ordinal()} is the id the client expects. {@link #NONE} is
 * transparent.</p>
 *
 * @since 0.1.0
 */
public enum MapColor {

    NONE(0x000000),
    GRASS(0x7FB238),
    SAND(0xF7E9A3),
    WOOL(0xC7C7C7),
    FIRE(0xFF0000),
    ICE(0xA0A0FF),
    METAL(0xA7A7A7),
    PLANT(0x007C00),
    SNOW(0xFFFFFF),
    CLAY(0xA4A8B8),
    DIRT(0x976D4D),
    STONE(0x707070),
    WATER(0x4040FF),
    WOOD(0x8F7748),
    QUARTZ(0xFFFCF5),
    COLOR_ORANGE(0xD87F33),
    COLOR_MAGENTA(0xB24CD8),
    COLOR_LIGHT_BLUE(0x6699D8),
    COLOR_YELLOW(0xE5E533),
    COLOR_LIGHT_GREEN(0x7FCC19),
    COLOR_PINK(0xF27FA5),
    COLOR_GRAY(0x4C4C4C),
    COLOR_LIGHT_GRAY(0x999999),
    COLOR_CYAN(0x4C7F99),
    COLOR_PURPLE(0x7F3FB2),
    COLOR_BLUE(0x334CB2),
    COLOR_BROWN(0x664C33),
    COLOR_GREEN(0x667F33),
    COLOR_RED(0x993333),
    COLOR_BLACK(0x191919),
    GOLD(0xFAEE4D),
    DIAMOND(0x5CDBD5),
    LAPIS(0x4A80FF),
    EMERALD(0x00D93A),
    PODZOL(0x815631),
    NETHER(0x700200),
    TERRACOTTA_WHITE(0xD1B1A1),
    TERRACOTTA_ORANGE(0x9F5224),
    TERRACOTTA_MAGENTA(0x95576C),
    TERRACOTTA_LIGHT_BLUE(0x706C8A),
    TERRACOTTA_YELLOW(0xBA8524),
    TERRACOTTA_LIGHT_GREEN(0x677535),
    TERRACOTTA_PINK(0xA04D4E),
    TERRACOTTA_GRAY(0x392923),
    TERRACOTTA_LIGHT_GRAY(0x876B62),
    TERRACOTTA_CYAN(0x575C5C),
    TERRACOTTA_PURPLE(0x7A4958),
    TERRACOTTA_BLUE(0x4C3E5C),
    TERRACOTTA_BROWN(0x4C3223),
    TERRACOTTA_GREEN(0x4C522A),
    TERRACOTTA_RED(0x8E3C2E),
    TERRACOTTA_BLACK(0x251610),
    CRIMSON_NYLIUM(0xBD3031),
    CRIMSON_STEM(0x943F61),
    CRIMSON_HYPHAE(0x5C191D),
    WARPED_NYLIUM(0x167E86),
    WARPED_STEM(0x3A8E8C),
    WARPED_HYPHAE(0x562C3E),
    WARPED_WART_BLOCK(0x14B485),
    DEEPSLATE(0x646464),
    RAW_IRON(0xD8AF93),
    GLOW_LICHEN(0x7FA796);

    private final int rgb;

    MapColor(final int rgb) {
        this.rgb = rgb;
    }

    /**
     * {@return the id the client knows this color by}
     *
     * @since 0.1.0
     */
    public int id() {
        return ordinal();
    }

    /**
     * {@return the color at full brightness, as {@code 0xRRGGBB}}
     *
     * <p>Meaningless for {@link #NONE}, which is drawn transparent.</p>
     *
     * @since 0.1.0
     */
    public int rgb() {
        return rgb;
    }
}
