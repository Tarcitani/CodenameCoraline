package com.codenamecoraline.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ThreadSafeLruCache}.
 */
@Execution(ExecutionMode.CONCURRENT)
class ThreadSafeLruCacheTest {

    @Test
    void basicPutAndGet() {
        ThreadSafeLruCache<String, Integer> cache = ThreadSafeLruCache.<String, Integer>builder().maxSize(3).build();

        assertNull(cache.get("a"));
        assertEquals(0, cache.size());

        cache.put("a", 1);
        assertEquals(1, cache.size());
        assertEquals(Integer.valueOf(1), cache.get("a"));

        cache.put("b", 2);
        cache.put("c", 3);
        assertEquals(3, cache.size());

        assertEquals(Integer.valueOf(1), cache.get("a"));
        assertEquals(Integer.valueOf(2), cache.get("b"));
        assertEquals(Integer.valueOf(3), cache.get("c"));
    }

    @Test
    void lruEviction() {
        ThreadSafeLruCache<String, Integer> cache = ThreadSafeLruCache.<String, Integer>builder().maxSize(3).build();

        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);

        // Access "a" to make it recently used
        cache.get("a");

        // Add "d" - should evict "b" (least recently used)
        cache.put("d", 4);

        assertEquals(3, cache.size());
        assertNull(cache.get("b")); // evicted
        assertEquals(Integer.valueOf(1), cache.get("a")); // still there
        assertEquals(Integer.valueOf(3), cache.get("c")); // still there
        assertEquals(Integer.valueOf(4), cache.get("d")); // new entry
    }

    @Test
    void getOrCompute() {
        ThreadSafeLruCache<String, Integer> cache = ThreadSafeLruCache.<String, Integer>builder().maxSize(10).build();
        AtomicInteger computeCount = new AtomicInteger(0);

        Integer result = cache.getOrCompute("key", k -> {
            computeCount.incrementAndGet();
            return 42;
        });

        assertEquals(Integer.valueOf(42), result);
        assertEquals(1, computeCount.get());

        // Second call should not recompute
        result = cache.getOrCompute("key", k -> {
            computeCount.incrementAndGet();
            return 99;
        });

        assertEquals(Integer.valueOf(42), result);
        assertEquals(1, computeCount.get());
    }

    @Test
    void getOrComputeWithNullReturn() {
        ThreadSafeLruCache<String, Integer> cache = ThreadSafeLruCache.<String, Integer>builder().maxSize(10).build();

        // Function returns null - should not cache
        Integer result = cache.getOrCompute("missing", k -> null);
        assertNull(result);
        assertFalse(cache.containsKey("missing"));
        assertEquals(0, cache.size());
    }

    @Test
    void remove() {
        ThreadSafeLruCache<String, Integer> cache = ThreadSafeLruCache.<String, Integer>builder().maxSize(10).build();

        cache.put("a", 1);
        cache.put("b", 2);

        assertEquals(Integer.valueOf(1), cache.remove("a"));
        assertNull(cache.get("a"));
        assertEquals(1, cache.size());

        assertNull(cache.remove("nonexistent"));
        assertEquals(1, cache.size());
    }

    @Test
    void clear() {
        ThreadSafeLruCache<String, Integer> cache = ThreadSafeLruCache.<String, Integer>builder().maxSize(10).build();

        cache.put("a", 1);
        cache.put("b", 2);
        cache.clear();

        assertTrue(cache.isEmpty());
        assertEquals(0, cache.size());
        assertNull(cache.get("a"));
    }

    @Test
    void snapshot() {
        ThreadSafeLruCache<String, Integer> cache = ThreadSafeLruCache.<String, Integer>builder().maxSize(3).build();

        cache.put("a", 1);
        cache.put("b", 2);
        cache.get("a"); // make "a" most recent

        var snapshot = cache.snapshot();
        assertEquals(2, snapshot.size());
        assertEquals(Integer.valueOf(1), snapshot.get("a"));
        assertEquals(Integer.valueOf(2), snapshot.get("b"));

        // Modifying snapshot should not affect cache
        snapshot.put("c", 3);
        assertEquals(2, cache.size());
        assertFalse(cache.containsKey("c"));
    }

    @Test
    void keysInLruOrder() {
        ThreadSafeLruCache<String, Integer> cache = ThreadSafeLruCache.<String, Integer>builder().maxSize(3).build();

        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);
        cache.get("a"); // "a" becomes most recent

        List<String> keys = cache.keysInLruOrder();
        // LRU order: least recent first -> b, c, a
        assertEquals(List.of("b", "c", "a"), keys);
    }

    @Test
    void isFull() {
        ThreadSafeLruCache<String, Integer> cache = ThreadSafeLruCache.<String, Integer>builder().maxSize(2).build();

        assertFalse(cache.isFull());
        cache.put("a", 1);
        assertFalse(cache.isFull());
        cache.put("b", 2);
        assertTrue(cache.isFull());
        cache.put("c", 3); // evicts "a"
        assertTrue(cache.isFull());
    }

    @Test
    void nullValueRejected() {
        ThreadSafeLruCache<String, Integer> cache = ThreadSafeLruCache.<String, Integer>builder().maxSize(10).build();

        assertThrows(NullPointerException.class, () -> cache.put("a", null));
    }

    @Test
    void nullMappingFunctionRejected() {
        ThreadSafeLruCache<String, Integer> cache = ThreadSafeLruCache.<String, Integer>builder().maxSize(10).build();

        assertThrows(NullPointerException.class, () -> cache.getOrCompute("a", null));
    }

    @Test
    void invalidMaxSizeRejected() {
        assertThrows(IllegalArgumentException.class, () -> ThreadSafeLruCache.builder().maxSize(0).build());
        assertThrows(IllegalArgumentException.class, () -> ThreadSafeLruCache.builder().maxSize(-1).build());
    }

    @Test
    void concurrentAccess() throws InterruptedException {
        ThreadSafeLruCache<Integer, Integer> cache = ThreadSafeLruCache.<Integer, Integer>builder().maxSize(1000).build();
        int threadCount = 16;
        int operationsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errors = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < operationsPerThread; i++) {
                        int key = threadId * operationsPerThread + i;
                        cache.put(key, key * 2);
                        Integer val = cache.get(key);
                        if (val != null && val != key * 2) {
                            errors.incrementAndGet();
                        }
                        // Also test getOrCompute
                        // Keep computed keys disjoint from every thread's explicit put keys.
                        // An overlapping namespace legitimately races two different values for one key.
                        cache.getOrCompute(key + threadCount * operationsPerThread, k -> k * 3);
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "Test timed out");
        executor.shutdown();

        assertEquals(0, errors.get(), "Concurrent access produced errors");
        assertTrue(cache.size() <= 1000, "Cache exceeded max size: " + cache.size());
    }

    @Test
    void concurrentGetOrCompute() throws InterruptedException {
        ThreadSafeLruCache<String, String> cache = ThreadSafeLruCache.<String, String>builder().maxSize(100).build();
        int threadCount = 32;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger computeCount = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < 100; i++) {
                        cache.getOrCompute("shared-key", k -> {
                            computeCount.incrementAndGet();
                            return "computed";
                        });
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        // Should compute exactly once despite concurrent access
        assertEquals(1, computeCount.get());
        assertEquals("computed", cache.get("shared-key"));
    }

    @Test
    void builderDefaults() {
        ThreadSafeLruCache<String, String> cache = ThreadSafeLruCache.<String, String>builder().build();
        assertEquals(256, cache.maxSize());
    }
}
