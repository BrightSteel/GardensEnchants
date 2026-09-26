package com.gardensmc.gardensenchants.utils;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class RomanNumber {

    private final static TreeMap<Integer, String> map = new TreeMap<Integer, String>();
//    private final static TreeMap<String, Integer> mapInversed = new TreeMap<String, Integer>();

    static {

        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");

//        for (Integer key : map.keySet()){
//            mapInversed.put(map.get(key), key);
//        }

    }

    public static String toRoman(int number) {
        if (number < 1) {
            return String.valueOf(number); // no numeral; used to throw on items with a bad level
        }
        int l =  map.floorKey(number);
        if ( number == l ) {
            return map.get(number);
        }
        return map.get(l) + toRoman(number-l);
    }

    public static int toNumber(String roman) {
        int num;
        switch (roman) {
            case "V" -> num = 5;
            case "IV" -> num = 4;
            case "III" -> num = 3;
            case "II" -> num = 2;
            default -> num = 1;
        }
        return num;
    }

}
