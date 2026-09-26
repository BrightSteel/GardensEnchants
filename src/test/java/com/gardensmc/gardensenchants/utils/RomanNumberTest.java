package com.gardensmc.gardensenchants.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RomanNumberTest {

    @Test
    void convertsLevels() {
        assertEquals("I", RomanNumber.toRoman(1));
        assertEquals("IV", RomanNumber.toRoman(4));
        assertEquals("IX", RomanNumber.toRoman(9));
        assertEquals("XIV", RomanNumber.toRoman(14));
        assertEquals("CCLV", RomanNumber.toRoman(255));
    }

    @Test
    void doesNotThrowBelowOne() {
        assertEquals("0", RomanNumber.toRoman(0));
        assertEquals("-3", RomanNumber.toRoman(-3));
    }
}
