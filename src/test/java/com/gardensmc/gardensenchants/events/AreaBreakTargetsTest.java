package com.gardensmc.gardensenchants.events;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AreaBreakTargetsTest {

    @Test
    void excludesOriginAndDuplicates() {
        var targets = AreaBreakTargets.select("origin", List.of("origin", "a", "b", "a", "origin", "c"), 64);
        assertEquals(List.of("a", "b", "c"), targets);
    }

    @Test
    void capsBlocksPerSwing() {
        var candidates = IntStream.range(0, 500).boxed().toList();
        var targets = AreaBreakTargets.select(0, candidates, 64);
        assertEquals(64, targets.size());
        assertFalse(targets.contains(0));
        assertEquals(1, targets.getFirst());
    }

    @Test
    void zeroCapBreaksNothing() {
        assertEquals(List.of(), AreaBreakTargets.select(0, List.of(1, 2, 3), 0));
    }
}
