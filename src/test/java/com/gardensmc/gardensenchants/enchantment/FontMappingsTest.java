package com.gardensmc.gardensenchants.enchantment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FontMappingsTest {

    @Test
    void shortNamesKeepTheirLevel() {
        assertEquals("Vein Miner II", FontMappings.fit("Vein Miner", " II"));
    }

    @Test
    void longNamesAreCutToTheRow() {
        var fitted = FontMappings.fit("Shepherd's Bounty", " III");
        assertTrue(fitted.startsWith("Shepherd's Bou") && fitted.endsWith("..."), fitted);
        assertTrue(FontMappings.getPixels(fitted) <= FontMappings.ROW_WIDTH);
    }

    @Test
    void vanillaWidths() {
        // i and ' are 1 wide, l 2, t and I 3, most 5; each plus a pixel; a space advances 4
        assertEquals(2 + 2 + 3 + 4 + 4 + 6, FontMappings.getPixels("i'l tA"));
    }
}
