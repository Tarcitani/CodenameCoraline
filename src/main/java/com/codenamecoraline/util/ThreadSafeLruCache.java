package com.codenamecoraline.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Thread-safe LRU (Least Recently Used) cache implementation for Minecraft mods.
 * <p>
 * Uses a synchronized {@link LinkedHashMap} with access-order to maintain LRU semantics.
 * All public operations are thread-safe. Designed for high-read, moderate-write workloads
 * typical in mod caching scenarios (texture lookups, entity data, config values).
 * </p>
 *
 * @param <K> key type
 * @param <V> value type
 */
public final class ThreadSafeLruCache<K, V> {

    private final LinkedHashMap<K, V> map;
    private final int maxSize;
    private final Object lock = new Object();

    /**
     * Creates a new LRU cache with the specified maximum size.
     *
     * @param maxSize maximum number of entries; must be > 0
     * @throws IllegalArgumentException if maxSize <= 0
     */
    public ThreadSafeLruCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be > 0, got: " + maxSize);
        }
        this.maxSize = maxSize;
        // accessOrder = true makes LinkedHashMap iterate in access order (LRU)
        this.map = new LinkedHashMap<>(maxSize, 0.75f, true);
    }

    /**
     * Gets a value from the cache, marking it as recently used.
     *
     * @param key the key to look up
     * @return the value, or {@code null} if not present
     */
    public V get(K key) {
        synchronized (lock) {
            return map.get(key);
        }
    }

    /**
     * Gets a value from the cache, or computes it if absent (atomic compute-if-absent).
     * <p>
     * The mapping function is executed while holding the lock, so it should be fast
     * and avoid blocking operations. For expensive computations, consider
     * {@link #getOrComputeAsync(K, Function)}.
     * </p>
     *
     * @param key the key to look up or compute
     * @param mappingFunction function to compute the value if absent
     * @return the existing or newly computed value
     * @throws NullPointerException if mappingFunction is null
     */
    public V getOrCompute(K key, Function<? super K, ? extends V> mappingFunction) {
        if (mappingFunction == null) {
            throw new NullPointerException("mappingFunction must not be null");
        }
        synchronized (lock) {
            V value = map.get(key);
            if (value != null) {
                return value;
            }
            V computed = mappingFunction.apply(key);
            if (computed != null) {
                putInternal(key, computed);
            }
            return computed;
        }
    }

    /**
     * Puts a key-value pair into the cache, evicting the LRU entry if necessary.
     *
     * @param key the key
     * @param value the value (must not be null)
     * @return the previous value associated with the key, or {@code null} if none
     * @throws NullPointerException if value is null
     */
    public V put(K key, V value) {
        if (value == null) {
            throw new NullPointerException("value must not be null");
        }
        synchronized (lock) {
            return putInternal(key, value);
        }
    }

    private V putInternal(K key, V value) {
        V previous = map.put(key, value);
        evictIfNeeded();
        return previous;
    }

    /**
     * Removes a key from the cache.
     *
     * @param key the key to remove
     * @return the removed value, or {@code null} if not present
     */
    public V remove(K key) {
        synchronized (lock) {
            return map.remove(key);
        }
    }

    /**
     * Checks if the cache contains a key.
     *
     * @param key the key to check
     * @return {@code true} if the key is present
     */
    public boolean containsKey(K key) {
        synchronized (lock) {
            return map.containsKey(key);
        }
    }

    /**
     * Returns the current number of entries in the cache.
     *
     * @return current size
     */
    public int size() {
        synchronized (lock) {
            return map.size();
        }
    }

    /**
     * Returns the maximum capacity of the cache.
     *
     * @return maximum size
     */
    public int maxSize() {
        return maxSize;
    }

    /**
     * Checks if the cache is empty.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        synchronized (lock) {
            return map.isEmpty();
        }
    }

    /**
     * Checks if the cache is at maximum capacity.
     *
     * @return {@code true} if full
     */
    public boolean isFull() {
        synchronized (lock) {
            return map.size() >= maxSize;
        }
    }

    /**
     * Clears all entries from the cache.
     */
    public void clear() {
        synchronized (lock) {
            map.clear();
        }
    }

    /**
     * Evicts the least recently used entry if the cache exceeds maxSize.
     * Called after each insertion.
     */
    private void evictIfNeeded() {
        if (map.size() > maxSize) {
            Map.Entry<K, V> eldest = map.entrySet().iterator().next();
            map.remove(eldest.getKey());
        }
    }

    /**
     * Returns a snapshot of the current cache contents (key -> value).
     * The returned map is a copy and modifications do not affect the cache.
     *
     * @return modifiable snapshot of cache entries in LRU order (least recent first)
     */
    public Map<K, V> snapshot() {
        synchronized (lock) {
            return new LinkedHashMap<>(map);
        }
    }

    /**
     * Returns a snapshot of keys in LRU order (least recently used first).
     *
     * @return unmodifiable list of keys
     */
    public java.util.List<K> keysInLruOrder() {
        synchronized (lock) {
            return java.util.List.copyOf(map.keySet());
        }
    }

    /**
     * Creates a new builder for configuring a ThreadSafeLruCache.
     *
     * @param <K> key type
     * @param <V> value type
     * @return a new builder
     */
    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    /**
     * Builder for {@link ThreadSafeLruCache}.
     *
     * @param <K> key type
     * @param <V> value type
     */
    public static final class Builder<K, V> {
        private int maxSize = 256;

        private Builder() {}

        /**
         * Sets the maximum cache size.
         *
         * @param maxSize maximum entries (> 0)
         * @return this builder
         */
        public Builder<K, V> maxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        /**
         * Builds the cache.
         *
         * @return a new ThreadSafeLruCache instance
         */
        public ThreadSafeLruCache<K, V> build() {
            return new ThreadSafeLruCache<>(maxSize);
        }
    }
}