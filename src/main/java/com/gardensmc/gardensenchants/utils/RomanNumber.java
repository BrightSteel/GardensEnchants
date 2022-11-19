package com.gardensmc.gardensenchants.utils;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class RomanNumber {

    private final static TreeMap<Integer, String> map = new TreeMap<Integer, String>();
    private final static TreeMap<String, Integer> mapInversed = new TreeMap<String, Integer>();

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

        for (Integer key : map.keySet()){
            mapInversed.put(map.get(key), key);
        }

    }

    public static String toRoman(int number) {
        int l =  map.floorKey(number);
        if ( number == l ) {
            return map.get(number);
        }
        return map.get(l) + toRoman(number-l);
    }

    public static int toNumber(String roman) {
        if (!mapInversed.containsKey(roman))
            return 1;
        return mapInversed.get(roman);
    }

}
