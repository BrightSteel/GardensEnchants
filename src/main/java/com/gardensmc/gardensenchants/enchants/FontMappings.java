package com.gardensmc.gardensenchants.enchants;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FontMappings {

    public static final FontMappings INSTANCE = new FontMappings();

    public final HashMap<Character, Character> fontMap1 = new HashMap<>();

    public Pattern pattern1;

    public FontMappings() {
        initMappings();
        initPattern1();
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
    }

    private void initPattern1() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(":(");
        for (char key : fontMap1.keySet()) {
            strBuilder.append(key);
            strBuilder.append("|");
        }
        String built = strBuilder.substring(0, strBuilder.length()-1);
        built += "):";
        pattern1 = Pattern.compile(built);
    }

    public String replaceAll(String text, HashMap<Character, Character> map) {
        char[] textChars = text.toCharArray();
        char[] mapped = new char[textChars.length];
        for (int i = 0; i < textChars.length; i++) {
            Character get = FontMappings.INSTANCE.fontMap1.get(textChars[i]);
            mapped[i] = (get != null) ? get : ' ';
        }
        return String.valueOf(mapped);
    }

}
