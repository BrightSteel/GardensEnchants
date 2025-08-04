package com.gardensmc.gardensenchants.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OnlinePlayerCache {

    public static OnlinePlayerCache INSTANCE = new OnlinePlayerCache();

    private final LoadingCache<UUID, OnlinePlayerData> cache;

    private OnlinePlayerCache() {
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(key -> new OnlinePlayerData());
    }

    public OnlinePlayerData getPlayerData(UUID uuid) {
        return cache.get(uuid);
    }
}
