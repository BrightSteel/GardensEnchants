package com.gardensmc.gardensenchants.probability;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public class ChanceResultPair<T> implements Comparable<ChanceResultPair<T>> {

    private final double chance;
    private final T result;

    @Override
    public int compareTo(@NotNull ChanceResultPair<T> o) {
        return Double.compare(chance, o.chance);
    }
}
