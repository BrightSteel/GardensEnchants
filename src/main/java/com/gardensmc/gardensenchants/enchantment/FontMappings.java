package com.gardensmc.gardensenchants.enchantment;

import com.gardensmc.gardensenchants.utils.FontShift;

/**
 * The enchanting table's offer names are typed as plain text in a font per row (gardens:enchant_row_1 to 3 in the
 * pack): vanilla's ascii.png at that row's height, so every printable ASCII character draws as in vanilla. Names are
 * measured with vanilla's glyph widths, to fit the row and to shift back to where the next row starts.
 */
public final class FontMappings {

    /** How wide an offer's row is, in pixels. */
    public static final int ROW_WIDTH = 84;
    private static final String ELLIPSIS = "...";
    // vanilla ascii.png: each printable character's glyph width in pixels, from ' ' (32) to '~' (126)
    private static final String GLYPH_WIDTHS =
            "31355551333515155555555555114545655555555355555555555555555353552555554551542555555535555553136";

    private FontMappings() {
    }

    public static String rowFont(int row) {
        return "gardens:enchant_row_" + row;
    }

    /** The text with anything ascii.png can't draw turned into a space. */
    public static String sanitize(String text) {
        var chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] < 32 || chars[i] > 126) chars[i] = ' ';
        }
        return String.valueOf(chars);
    }

    /** The name and level if they fit the row; else as much of the name as fits, then "...". */
    public static String fit(String name, String level) {
        var full = sanitize(name + level);
        if (getPixels(full) <= ROW_WIDTH) return full;
        var cut = sanitize(name);
        while (!cut.isEmpty() && getPixels(cut.stripTrailing() + ELLIPSIS) > ROW_WIDTH) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut.stripTrailing() + ELLIPSIS;
    }

    // a glyph's width and the pixel after it (a space advances 4)
    private static int getPixels(char c) {
        int width = c >= 32 && c <= 126 ? GLYPH_WIDTHS.charAt(c - 32) - '0' : 5;
        return width + 1;
    }

    static int getPixels(String s) {
        int pixels = 0;
        for (char c : s.toCharArray()) {
            pixels += getPixels(c);
        }
        return pixels;
    }

    public static String getPadding(String s) {
        return FontShift.INSTANCE.getShift(-getPixels(s));
    }
}
