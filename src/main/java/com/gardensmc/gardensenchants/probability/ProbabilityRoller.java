package com.gardensmc.gardensenchants.probability;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor
public class ProbabilityRoller<T> {
    private List<ChanceResultPair<T>> chanceResultPairs;
    private T defaultResult;

    public ProbabilityRoller() {

    }

    public T roll() {
        var num = ThreadLocalRandom.current().nextInt(100);
        var resultPair = chanceResultPairs.stream()
                .sorted()
                .filter(chanceResultPair -> chanceResultPair.getChance() > num)
                .findFirst().
                orElse(null);
        return resultPair != null ? resultPair.getResult() : defaultResult;
    }

}

