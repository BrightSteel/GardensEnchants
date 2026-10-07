package com.gardensmc.gardensenchants.gui;

import org.junit.jupiter.api.Test;

import static com.gardensmc.gardensenchants.gui.RerollPricing.DECAY_MS;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RerollPricingTest {

    @Test
    void getsDearerWithEachRerollInARow() {
        assertEquals(40, RerollPricing.price(25, 40, null, 0));
        var streak = RerollPricing.rerolled(null, 0);
        assertEquals(50, RerollPricing.price(25, 40, streak, 1));
        streak = RerollPricing.rerolled(streak, 1);
        assertEquals(63, RerollPricing.price(25, 40, streak, 2));
        assertEquals(117, RerollPricing.price(75, 40, streak, 2));
    }

    @Test
    void comesDownOneStepPerQuarterHour() {
        var streak = new RerollPricing.Streak(4, 0);
        assertEquals(4, RerollPricing.rerolls(streak, DECAY_MS - 1));
        assertEquals(3, RerollPricing.rerolls(streak, DECAY_MS));
        assertEquals(1, RerollPricing.rerolls(streak, 3 * DECAY_MS));
        assertEquals(0, RerollPricing.rerolls(streak, 10 * DECAY_MS));
        // a re-roll after waiting builds on what's left
        assertEquals(2, RerollPricing.rerolled(streak, 3 * DECAY_MS).rerolls());
    }
}
