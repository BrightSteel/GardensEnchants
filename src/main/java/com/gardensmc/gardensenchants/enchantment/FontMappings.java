package com.gardensmc.gardensenchants.enchantment;

import com.gardensmc.gardensenchants.utils.FontShift;

public final class FontMappings {

    private FontMappings() {
    }

    /**
     * The enchanting table's offer names are typed as plain text in a font per row (gardens:enchant_row_1 to 3 in the
     * pack), which has only letters, '.' and space. This is the font for that row, and the text with anything else
     * turned into a space.
     */
    public static String rowFont(int row) {
        return "gardens:enchant_row_" + row;
    }

    public static String sanitize(String text) {
        var chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            boolean drawn = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '.';
            if (!drawn) chars[i] = ' ';
        }
        return String.valueOf(chars);
    }

    private static int getPixels(char c) {
        int pixels;
        switch (c) {
            default -> pixels = 5;
            case 'I', 't', ' ' -> pixels = 3;
            case 'f', 'k' -> pixels = 4;
            case 'l' -> pixels = 2;
            case 'i', '.' -> pixels = 1;

        }
        pixels++; //account for space between letters
        return pixels;
    }

    private static int getPixels(String s) {

        int pixels = 0;
        char[] chars = s.toCharArray();
        for (char c : chars) {
            pixels += getPixels(c);
        }
        return pixels;
    }

    public static String getPadding(String s) {
        int pixels = getPixels(s);
        String shift = FontShift.INSTANCE.getShift(-pixels);
        return shift;
    }

}
