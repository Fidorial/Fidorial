package fr.euphyllia.fidorial.server.map;

import fr.fidorial.map.MapFont;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * The built-in font, see {@link fr.fidorial.map.MapManager#standardFont()}.
 *
 * <p>An original design: glyphs are 7 rows high, lowercase letters 5 rows high above the baseline
 * with descenders folded in, accents of capitals two rows above. Rows are stored as bit masks, bit
 * {@code n} for column {@code n}.</p>
 */
final class StandardMapFont implements MapFont {

    private static final int LINE_HEIGHT = 10;
    private static final int ASCENT = 2;
    private static final Glyph SPACE = new Glyph(3, 0, new int[0]);

    // declared after the constants the constructor uses
    static final StandardMapFont INSTANCE = new StandardMapFont();

    private final Glyph[] latin = new Glyph[0x180];
    private final Map<Integer, Glyph> others = new HashMap<>();

    private StandardMapFont() {
        latin[' '] = SPACE;
        latin[0xA0] = SPACE;
        glyph(0x0021, 1, 0, 1, 1, 1, 1, 1, 0, 1);
        glyph(0x0022, 3, 0, 5, 5, 0, 0, 0, 0, 0);
        glyph(0x0023, 5, 0, 10, 10, 31, 10, 31, 10, 10);
        glyph(0x0024, 5, 0, 4, 30, 5, 14, 20, 15, 4);
        glyph(0x0025, 5, 0, 3, 19, 8, 4, 2, 25, 24);
        glyph(0x0026, 5, 0, 6, 9, 5, 2, 21, 9, 22);
        glyph(0x0027, 1, 0, 1, 1, 0, 0, 0, 0, 0);
        glyph(0x0028, 3, 0, 4, 2, 1, 1, 1, 2, 4);
        glyph(0x0029, 3, 0, 1, 2, 4, 4, 4, 2, 1);
        glyph(0x002A, 5, 0, 0, 4, 21, 14, 21, 4, 0);
        glyph(0x002B, 5, 0, 0, 4, 4, 31, 4, 4, 0);
        glyph(0x002C, 2, 0, 0, 0, 0, 0, 0, 2, 1);
        glyph(0x002D, 5, 0, 0, 0, 0, 31, 0, 0, 0);
        glyph(0x002E, 1, 0, 0, 0, 0, 0, 0, 0, 1);
        glyph(0x002F, 5, 0, 16, 16, 8, 4, 2, 1, 1);
        glyph(0x0030, 5, 0, 14, 17, 25, 21, 19, 17, 14);
        glyph(0x0031, 3, 0, 2, 3, 2, 2, 2, 2, 7);
        glyph(0x0032, 5, 0, 14, 17, 16, 12, 2, 1, 31);
        glyph(0x0033, 5, 0, 14, 17, 16, 12, 16, 17, 14);
        glyph(0x0034, 5, 0, 8, 12, 10, 9, 31, 8, 8);
        glyph(0x0035, 5, 0, 31, 1, 15, 16, 16, 17, 14);
        glyph(0x0036, 5, 0, 12, 2, 1, 15, 17, 17, 14);
        glyph(0x0037, 5, 0, 31, 16, 8, 4, 2, 2, 2);
        glyph(0x0038, 5, 0, 14, 17, 17, 14, 17, 17, 14);
        glyph(0x0039, 5, 0, 14, 17, 17, 30, 16, 8, 6);
        glyph(0x003A, 1, 0, 0, 0, 1, 0, 0, 1, 0);
        glyph(0x003B, 2, 0, 0, 0, 2, 0, 0, 2, 1);
        glyph(0x003C, 4, 0, 8, 4, 2, 1, 2, 4, 8);
        glyph(0x003D, 5, 0, 0, 0, 31, 0, 31, 0, 0);
        glyph(0x003E, 4, 0, 1, 2, 4, 8, 4, 2, 1);
        glyph(0x003F, 5, 0, 14, 17, 16, 8, 4, 0, 4);
        glyph(0x0040, 5, 0, 14, 17, 29, 21, 29, 1, 30);
        glyph(0x0041, 5, 0, 14, 17, 17, 31, 17, 17, 17);
        glyph(0x0042, 5, 0, 15, 17, 17, 15, 17, 17, 15);
        glyph(0x0043, 5, 0, 14, 17, 1, 1, 1, 17, 14);
        glyph(0x0044, 5, 0, 15, 17, 17, 17, 17, 17, 15);
        glyph(0x0045, 5, 0, 31, 1, 1, 15, 1, 1, 31);
        glyph(0x0046, 5, 0, 31, 1, 1, 15, 1, 1, 1);
        glyph(0x0047, 5, 0, 14, 17, 1, 29, 17, 17, 30);
        glyph(0x0048, 5, 0, 17, 17, 17, 31, 17, 17, 17);
        glyph(0x0049, 3, 0, 7, 2, 2, 2, 2, 2, 7);
        glyph(0x004A, 5, 0, 16, 16, 16, 16, 17, 17, 14);
        glyph(0x004B, 5, 0, 17, 9, 5, 3, 5, 9, 17);
        glyph(0x004C, 5, 0, 1, 1, 1, 1, 1, 1, 31);
        glyph(0x004D, 5, 0, 17, 27, 21, 21, 17, 17, 17);
        glyph(0x004E, 5, 0, 17, 19, 21, 25, 17, 17, 17);
        glyph(0x004F, 5, 0, 14, 17, 17, 17, 17, 17, 14);
        glyph(0x0050, 5, 0, 15, 17, 17, 15, 1, 1, 1);
        glyph(0x0051, 5, 0, 14, 17, 17, 17, 21, 9, 22);
        glyph(0x0052, 5, 0, 15, 17, 17, 15, 5, 9, 17);
        glyph(0x0053, 5, 0, 30, 1, 1, 14, 16, 16, 15);
        glyph(0x0054, 5, 0, 31, 4, 4, 4, 4, 4, 4);
        glyph(0x0055, 5, 0, 17, 17, 17, 17, 17, 17, 14);
        glyph(0x0056, 5, 0, 17, 17, 17, 17, 17, 10, 4);
        glyph(0x0057, 5, 0, 17, 17, 17, 21, 21, 27, 17);
        glyph(0x0058, 5, 0, 17, 17, 10, 4, 10, 17, 17);
        glyph(0x0059, 5, 0, 17, 17, 10, 4, 4, 4, 4);
        glyph(0x005A, 5, 0, 31, 16, 8, 4, 2, 1, 31);
        glyph(0x005B, 3, 0, 7, 1, 1, 1, 1, 1, 7);
        glyph(0x005C, 5, 0, 1, 1, 2, 4, 8, 16, 16);
        glyph(0x005D, 3, 0, 7, 4, 4, 4, 4, 4, 7);
        glyph(0x005E, 5, 0, 4, 10, 17, 0, 0, 0, 0);
        glyph(0x005F, 5, 0, 0, 0, 0, 0, 0, 0, 31);
        glyph(0x0060, 2, 0, 1, 2, 0, 0, 0, 0, 0);
        glyph(0x0061, 5, 0, 0, 0, 14, 16, 30, 17, 30);
        glyph(0x0062, 5, 0, 1, 1, 13, 19, 17, 17, 15);
        glyph(0x0063, 5, 0, 0, 0, 14, 1, 1, 17, 14);
        glyph(0x0064, 5, 0, 16, 16, 22, 25, 17, 17, 30);
        glyph(0x0065, 5, 0, 0, 0, 14, 17, 31, 1, 14);
        glyph(0x0066, 4, 0, 12, 2, 15, 2, 2, 2, 2);
        glyph(0x0067, 5, 0, 0, 30, 17, 17, 30, 16, 14);
        glyph(0x0068, 5, 0, 1, 1, 13, 19, 17, 17, 17);
        glyph(0x0069, 1, 0, 1, 0, 1, 1, 1, 1, 1);
        glyph(0x006A, 4, 0, 8, 0, 12, 8, 8, 9, 6);
        glyph(0x006B, 4, 0, 1, 1, 9, 5, 3, 5, 9);
        glyph(0x006C, 2, 0, 1, 1, 1, 1, 1, 1, 2);
        glyph(0x006D, 5, 0, 0, 0, 11, 21, 21, 21, 21);
        glyph(0x006E, 5, 0, 0, 0, 13, 19, 17, 17, 17);
        glyph(0x006F, 5, 0, 0, 0, 14, 17, 17, 17, 14);
        glyph(0x0070, 5, 0, 0, 0, 15, 17, 15, 1, 1);
        glyph(0x0071, 5, 0, 0, 0, 30, 17, 30, 16, 16);
        glyph(0x0072, 5, 0, 0, 0, 13, 19, 1, 1, 1);
        glyph(0x0073, 5, 0, 0, 0, 30, 1, 14, 16, 15);
        glyph(0x0074, 4, 0, 2, 2, 15, 2, 2, 2, 12);
        glyph(0x0075, 5, 0, 0, 0, 17, 17, 17, 25, 22);
        glyph(0x0076, 5, 0, 0, 0, 17, 17, 17, 10, 4);
        glyph(0x0077, 5, 0, 0, 0, 17, 17, 21, 21, 10);
        glyph(0x0078, 5, 0, 0, 0, 17, 10, 4, 10, 17);
        glyph(0x0079, 5, 0, 0, 0, 17, 17, 30, 16, 14);
        glyph(0x007A, 5, 0, 0, 0, 31, 8, 4, 2, 31);
        glyph(0x007B, 3, 0, 4, 2, 2, 1, 2, 2, 4);
        glyph(0x007C, 1, 0, 1, 1, 1, 1, 1, 1, 1);
        glyph(0x007D, 3, 0, 1, 2, 2, 4, 2, 2, 1);
        glyph(0x007E, 5, 0, 0, 0, 2, 21, 8, 0, 0);
        glyph(0x00AB, 6, 0, 0, 0, 36, 18, 9, 18, 36);
        glyph(0x00B0, 3, 0, 2, 5, 2, 0, 0, 0, 0);
        glyph(0x00BB, 6, 0, 0, 0, 9, 18, 36, 18, 9);
        glyph(0x00C0, 5, -2, 2, 4, 14, 17, 17, 31, 17, 17, 17);
        glyph(0x00C1, 5, -2, 8, 4, 14, 17, 17, 31, 17, 17, 17);
        glyph(0x00C2, 5, -2, 4, 10, 14, 17, 17, 31, 17, 17, 17);
        glyph(0x00C3, 5, -2, 20, 10, 14, 17, 17, 31, 17, 17, 17);
        glyph(0x00C4, 5, -2, 10, 0, 14, 17, 17, 31, 17, 17, 17);
        glyph(0x00C7, 5, 0, 14, 17, 1, 1, 1, 17, 14, 4);
        glyph(0x00C8, 5, -2, 2, 4, 31, 1, 1, 15, 1, 1, 31);
        glyph(0x00C9, 5, -2, 8, 4, 31, 1, 1, 15, 1, 1, 31);
        glyph(0x00CA, 5, -2, 4, 10, 31, 1, 1, 15, 1, 1, 31);
        glyph(0x00CB, 5, -2, 10, 0, 31, 1, 1, 15, 1, 1, 31);
        glyph(0x00CC, 3, -2, 1, 2, 7, 2, 2, 2, 2, 2, 7);
        glyph(0x00CD, 3, -2, 4, 2, 7, 2, 2, 2, 2, 2, 7);
        glyph(0x00CE, 3, -2, 2, 5, 7, 2, 2, 2, 2, 2, 7);
        glyph(0x00CF, 3, -2, 5, 0, 7, 2, 2, 2, 2, 2, 7);
        glyph(0x00D1, 5, -2, 20, 10, 17, 19, 21, 25, 17, 17, 17);
        glyph(0x00D2, 5, -2, 2, 4, 14, 17, 17, 17, 17, 17, 14);
        glyph(0x00D3, 5, -2, 8, 4, 14, 17, 17, 17, 17, 17, 14);
        glyph(0x00D4, 5, -2, 4, 10, 14, 17, 17, 17, 17, 17, 14);
        glyph(0x00D5, 5, -2, 20, 10, 14, 17, 17, 17, 17, 17, 14);
        glyph(0x00D6, 5, -2, 10, 0, 14, 17, 17, 17, 17, 17, 14);
        glyph(0x00D9, 5, -2, 2, 4, 17, 17, 17, 17, 17, 17, 14);
        glyph(0x00DA, 5, -2, 8, 4, 17, 17, 17, 17, 17, 17, 14);
        glyph(0x00DB, 5, -2, 4, 10, 17, 17, 17, 17, 17, 17, 14);
        glyph(0x00DC, 5, -2, 10, 0, 17, 17, 17, 17, 17, 17, 14);
        glyph(0x00E0, 5, 0, 2, 4, 14, 16, 30, 17, 30);
        glyph(0x00E1, 5, 0, 8, 4, 14, 16, 30, 17, 30);
        glyph(0x00E2, 5, 0, 4, 10, 14, 16, 30, 17, 30);
        glyph(0x00E3, 5, 0, 20, 10, 14, 16, 30, 17, 30);
        glyph(0x00E4, 5, 0, 0, 10, 14, 16, 30, 17, 30);
        glyph(0x00E7, 5, 0, 0, 0, 14, 1, 1, 17, 14, 4);
        glyph(0x00E8, 5, 0, 2, 4, 14, 17, 31, 1, 14);
        glyph(0x00E9, 5, 0, 8, 4, 14, 17, 31, 1, 14);
        glyph(0x00EA, 5, 0, 4, 10, 14, 17, 31, 1, 14);
        glyph(0x00EB, 5, 0, 0, 10, 14, 17, 31, 1, 14);
        glyph(0x00EC, 3, 0, 1, 2, 2, 2, 2, 2, 2);
        glyph(0x00ED, 3, 0, 4, 2, 2, 2, 2, 2, 2);
        glyph(0x00EE, 3, 0, 2, 5, 2, 2, 2, 2, 2);
        glyph(0x00EF, 3, 0, 0, 5, 2, 2, 2, 2, 2);
        glyph(0x00F1, 5, 0, 20, 10, 13, 19, 17, 17, 17);
        glyph(0x00F2, 5, 0, 2, 4, 14, 17, 17, 17, 14);
        glyph(0x00F3, 5, 0, 8, 4, 14, 17, 17, 17, 14);
        glyph(0x00F4, 5, 0, 4, 10, 14, 17, 17, 17, 14);
        glyph(0x00F5, 5, 0, 20, 10, 14, 17, 17, 17, 14);
        glyph(0x00F6, 5, 0, 0, 10, 14, 17, 17, 17, 14);
        glyph(0x00F9, 5, 0, 2, 4, 17, 17, 17, 25, 22);
        glyph(0x00FA, 5, 0, 8, 4, 17, 17, 17, 25, 22);
        glyph(0x00FB, 5, 0, 4, 10, 17, 17, 17, 25, 22);
        glyph(0x00FC, 5, 0, 0, 10, 17, 17, 17, 25, 22);
        glyph(0x00FF, 5, 0, 0, 10, 17, 17, 30, 16, 14);
        glyph(0x0153, 5, 0, 0, 0, 10, 21, 29, 5, 26);
        glyph(0x0178, 5, -2, 10, 0, 17, 17, 10, 4, 4, 4, 4);
        glyph(0x20AC, 5, 0, 28, 2, 15, 2, 15, 2, 28);
    }

    private void glyph(final int codePoint, final int width, final int top, final int... rows) {
        final Glyph glyph = new Glyph(width, top, rows);
        if (codePoint < latin.length) {
            latin[codePoint] = glyph;
        } else {
            others.put(codePoint, glyph);
        }
    }

    @Override
    public @Nullable Glyph glyph(final int codePoint) {
        if (codePoint >= 0 && codePoint < latin.length) {
            return latin[codePoint];
        }
        return others.get(codePoint);
    }

    @Override
    public int lineHeight() {
        return LINE_HEIGHT;
    }

    @Override
    public int ascent() {
        return ASCENT;
    }

    @Override
    public int letterSpacing() {
        return 1;
    }

    @Override
    public int width(final String text) {
        return MapFonts.width(this, text);
    }

    @Override
    public Glyph fallback() {
        return MapFonts.BOX;
    }
}
