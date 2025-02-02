package com.gardensmc.gardensenchants.cache;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public abstract class AsyncCache<K, V> {

    private final AsyncLoadingCache<K, V> cache;

    public AsyncCache() {
        this(5);
    }

    public AsyncCache(int expirationMinutes) {
        cache = Caffeine.newBuilder()
                .expireAfterAccess(expirationMinutes, TimeUnit.MINUTES)
                .buildAsync((key, executor) -> createCacheEntry(key));
    }

    public CompletableFuture<V> get(K key) {
        return cache.get(key);
    }

    public void remove(K key) {
        cache.synchronous().invalidate(key);
    }

    public void removeAll() {
        cache.synchronous().invalidateAll();
    }

    protected abstract CompletableFuture<V> createCacheEntry(K key);

}
