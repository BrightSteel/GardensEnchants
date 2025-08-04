package com.gardensmc.gardensenchants.enchantment;

import com.gardensmc.gardensenchants.utils.FontShift;

import java.util.HashMap;

public class FontMappings {

    public static final FontMappings INSTANCE = new FontMappings();

    public final HashMap<Character, Character> fontMap1 = new HashMap<>();
    public final HashMap<Character, Character> fontMap2 = new HashMap<>();
    public final HashMap<Character, Character> fontMap3 = new HashMap<>();


    public FontMappings() {
        initMappings();
    }

    private void initMappings() {
        fontMap1.put('A','\uE060');
        fontMap1.put('B', '\uE061');
        fontMap1.put('C', '\uE062');
        fontMap1.put('D', '\uE063');
        fontMap1.put('E', '\uE064');
        fontMap1.put('F', '\uE065');
        fontMap1.put('G', '\uE066');
        fontMap1.put('H', '\uE067');
        fontMap1.put('I', '\uE068');
        fontMap1.put('J', '\uE069');
        fontMap1.put('K', '\uE06A');
        fontMap1.put('L', '\uE06B');
        fontMap1.put('M', '\uE06C');
        fontMap1.put('N', '\uE06D');
        fontMap1.put('O', '\uE06E');
        fontMap1.put('P', '\uE06F');
        fontMap1.put('Q', '\uE070');
        fontMap1.put('R', '\uE071');
        fontMap1.put('S', '\uE072');
        fontMap1.put('T', '\uE073');
        fontMap1.put('U', '\uE074');
        fontMap1.put('V', '\uE075');
        fontMap1.put('W', '\uE076');
        fontMap1.put('X', '\uE077');
        fontMap1.put('Y', '\uE078');
        fontMap1.put('Z', '\uE079');
        fontMap1.put('a', '\uE07A');
        fontMap1.put('b', '\uE07B');
        fontMap1.put('c', '\uE07C');
        fontMap1.put('d', '\uE07D');
        fontMap1.put('e', '\uE07E');
        fontMap1.put('f', '\uE07F');
        fontMap1.put('g', '\uE080');
        fontMap1.put('h', '\uE081');
        fontMap1.put('i', '\uE082');
        fontMap1.put('j', '\uE083');
        fontMap1.put('k', '\uE084');
        fontMap1.put('l', '\uE085');
        fontMap1.put('m', '\uE086');
        fontMap1.put('n', '\uE087');
        fontMap1.put('o', '\uE088');
        fontMap1.put('p', '\uE089');
        fontMap1.put('q', '\uE08A');
        fontMap1.put('r', '\uE08B');
        fontMap1.put('s', '\uE08C');
        fontMap1.put('t', '\uE08D');
        fontMap1.put('u', '\uE08E');
        fontMap1.put('v', '\uE08F');
        fontMap1.put('w', '\uE090');
        fontMap1.put('x', '\uE091');
        fontMap1.put('y', '\uE092');
        fontMap1.put('z', '\uE093');
        fontMap1.put('.', '\uE259');

        fontMap2.put('A', '\uE096');
        fontMap2.put('B', '\uE097');
        fontMap2.put('C', '\uE098');
        fontMap2.put('D', '\uE099');
        fontMap2.put('E', '\uE09A');
        fontMap2.put('F', '\uE09B');
        fontMap2.put('G', '\uE09C');
        fontMap2.put('H', '\uE09D');
        fontMap2.put('I', '\uE09E');
        fontMap2.put('J', '\uE09F');
        fontMap2.put('K', '\uE0A0');
        fontMap2.put('L', '\uE0A1');
        fontMap2.put('M', '\uE0A2');
        fontMap2.put('N', '\uE0A3');
        fontMap2.put('O', '\uE0A4');
        fontMap2.put('P', '\uE0A5');
        fontMap2.put('Q', '\uE0A6');
        fontMap2.put('R', '\uE0A7');
        fontMap2.put('S', '\uE0A8');
        fontMap2.put('T', '\uE0A9');
        fontMap2.put('U', '\uE0AA');
        fontMap2.put('V', '\uE0AB');
        fontMap2.put('W', '\uE0AC');
        fontMap2.put('X', '\uE0AD');
        fontMap2.put('Y', '\uE0AE');
        fontMap2.put('Z', '\uE0AF');
        fontMap2.put('a', '\uE0B0');
        fontMap2.put('b', '\uE0B1');
        fontMap2.put('c', '\uE0B2');
        fontMap2.put('d', '\uE0B3');
        fontMap2.put('e', '\uE0B4');
        fontMap2.put('f', '\uE0B5');
        fontMap2.put('g', '\uE0B6');
        fontMap2.put('h', '\uE0B7');
        fontMap2.put('i', '\uE0B8');
        fontMap2.put('j', '\uE0B9');
        fontMap2.put('k', '\uE0BA');
        fontMap2.put('l', '\uE0BB');
        fontMap2.put('m', '\uE0BC');
        fontMap2.put('n', '\uE0BD');
        fontMap2.put('o', '\uE0BE');
        fontMap2.put('p', '\uE0BF');
        fontMap2.put('q', '\uE0C0');
        fontMap2.put('r', '\uE0C1');
        fontMap2.put('s', '\uE0C2');
        fontMap2.put('t', '\uE0C3');
        fontMap2.put('u', '\uE0C4');
        fontMap2.put('v', '\uE0C5');
        fontMap2.put('w', '\uE0C6');
        fontMap2.put('x', '\uE0C7');
        fontMap2.put('y', '\uE0C8');
        fontMap2.put('z', '\uE0C9');
        fontMap2.put('.', '\uE257');

        fontMap3.put('A', '\uE0CA');
        fontMap3.put('B', '\uE0CB');
        fontMap3.put('C', '\uE0CC');
        fontMap3.put('D', '\uE0CD');
        fontMap3.put('E', '\uE0CE');
        fontMap3.put('F', '\uE0CF');
        fontMap3.put('G', '\uE0D0');
        fontMap3.put('H', '\uE0D1');
        fontMap3.put('I', '\uE0D2');
        fontMap3.put('J', '\uE0D3');
        fontMap3.put('K', '\uE0D4');
        fontMap3.put('L', '\uE0D5');
        fontMap3.put('M', '\uE0D6');
        fontMap3.put('N', '\uE0D7');
        fontMap3.put('O', '\uE0D8');
        fontMap3.put('P', '\uE0D9');
        fontMap3.put('Q', '\uE0DA');
        fontMap3.put('R', '\uE0DB');
        fontMap3.put('S', '\uE0DC');
        fontMap3.put('T', '\uE0DD');
        fontMap3.put('U', '\uE0DE');
        fontMap3.put('V', '\uE0DF');
        fontMap3.put('W', '\uE0E0');
        fontMap3.put('X', '\uE0E1');
        fontMap3.put('Y', '\uE0E2');
        fontMap3.put('Z', '\uE0E3');
        fontMap3.put('a', '\uE0E4');
        fontMap3.put('b', '\uE0E5');
        fontMap3.put('c', '\uE0E6');
        fontMap3.put('d', '\uE0E7');
        fontMap3.put('e', '\uE0E8');
        fontMap3.put('f', '\uE0E9');
        fontMap3.put('g', '\uE0EA');
        fontMap3.put('h', '\uE0EB');
        fontMap3.put('i', '\uE0EC');
        fontMap3.put('j', '\uE0ED');
        fontMap3.put('k', '\uE0EE');
        fontMap3.put('l', '\uE0EF');
        fontMap3.put('m', '\uE0F0');
        fontMap3.put('n', '\uE0F1');
        fontMap3.put('o', '\uE0F2');
        fontMap3.put('p', '\uE0F3');
        fontMap3.put('q', '\uE0F4');
        fontMap3.put('r', '\uE0F5');
        fontMap3.put('s', '\uE0F6');
        fontMap3.put('t', '\uE0F7');
        fontMap3.put('u', '\uE0F8');
        fontMap3.put('v', '\uE0F9');
        fontMap3.put('w', '\uE0FA');
        fontMap3.put('x', '\uE0FB');
        fontMap3.put('y', '\uE0FC');
        fontMap3.put('z', '\uE0FD');
        fontMap3.put('.', '\uE258');
    }

    public String replaceAll(String text, HashMap<Character, Character> map) {
        char[] textChars = text.toCharArray();
        char[] mapped = new char[textChars.length];
        for (int i = 0; i < textChars.length; i++) {
            Character get = map.get(textChars[i]);
            mapped[i] = (get != null) ? get : ' ';
        }
        return String.valueOf(mapped);
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
//         gauranteed to have one space, so we don't care about that one
//        int spaceCount = s.split(" ").length - 1 - 1;

        int pixels = 0;
        char[] chars = s.toCharArray();
        for (char c : chars) {
            pixels += getPixels(c);
        }
//        pixels -= spaceCount;
        return pixels;
    }

    public static String getPadding(String s) {
//        System.out.println("Filling this to " + s);
        int pixels = getPixels(s);
//        int pad = width - pixels;
//        boolean neg = (pad < 0);
//        System.out.println("Getting pad for " + pad);
        String shift = FontShift.INSTANCE.getShift(-pixels);
        return shift;
//        return ":offset_" + pad + ":";
    }

}
