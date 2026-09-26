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

    /**
     * The value if it has finished loading successfully, otherwise null. Never waits.
     */
    public V getIfLoaded(K key) {
        var future = cache.getIfPresent(key);
        if (future == null || !future.isDone() || future.isCompletedExceptionally()) {
            return null;
        }
        return future.join();
    }

    public void put(K key, V value) {
        cache.put(key, CompletableFuture.completedFuture(value));
    }

    public void remove(K key) {
        cache.synchronous().invalidate(key);
    }

    protected abstract CompletableFuture<V> createCacheEntry(K key);

}
