package com.gardensmc.gardensenchants.gui;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RerollPricingTest {

    private final RerollPricing pricing = new RerollPricingForTest();
    private final UUID player = UUID.randomUUID();

    @Test
    void getsDearerWithEachRerollInARow() {
        assertEquals(25, pricing.price(player, 25, 0));
        pricing.rerolled(player, 0);
        assertEquals(31, pricing.price(player, 25, 1));
        pricing.rerolled(player, 1);
        assertEquals(39, pricing.price(player, 25, 2));
    }

    @Test
    void resetsAfterABreak() {
        pricing.rerolled(player, 0);
        pricing.rerolled(player, 0);
        assertEquals(25, pricing.price(player, 25, RerollPricing.RESET_AFTER_MS));
    }

    @Test
    void cheapPurchaseDoesNotReset() {
        pricing.rerolled(player, 0);
        pricing.rerolled(player, 0);
        pricing.rerolled(player, 0);
        pricing.bought(player, 25, pricing.price(player, 25, 0));
        assertEquals(3, pricing.rerolls(player, 0));
        pricing.bought(player, 300, pricing.price(player, 25, 0));
        assertEquals(0, pricing.rerolls(player, 0));
    }

    /** A fresh one per test, not the shared instance. */
    private static final class RerollPricingForTest extends RerollPricing {
    }
}
