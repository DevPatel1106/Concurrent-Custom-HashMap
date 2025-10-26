package concurrentmap;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Benchmark performance of different concurrent map implementations:
 * - ConcurrentCustomMap with Bucket, LockFreeBucket, TreeBucket
 * - ResizableConcurrentMap (optional)
 * - Standard Java ConcurrentHashMap and SynchronizedMap
 */
public class BenchmarkTest {

    private static final int THREAD_COUNT = 8;
    private static final int OPERATIONS_PER_THREAD = 100_000;
    private static final double READ_RATIO = 0.8; // 80% reads

    // Helper class to hold benchmark results
    private static class BenchmarkResult {
        long totalOps;
        long totalTimeNs;
        double opsPerSec;
        double avgLatency;

        BenchmarkResult(long totalOps, long totalTimeNs, double opsPerSec, double avgLatency) {
            this.totalOps = totalOps;
            this.totalTimeNs = totalTimeNs;
            this.opsPerSec = opsPerSec;
            this.avgLatency = avgLatency;
        }
    }

    @Test
    void runFullBenchmarkSuite() throws InterruptedException {
        System.out.println("===== CONCURRENT MAP PERFORMANCE BENCHMARK =====");
        System.out.printf("Threads: %d | Operations: %,d | Read Ratio: %.0f%%%n%n",
                THREAD_COUNT, OPERATIONS_PER_THREAD, READ_RATIO * 100);

        // Modular buckets
        benchmarkCustom("Bucket", Bucket::new);
        benchmarkCustom("LockFreeBucket", LockFreeBucket::new);
        benchmarkCustom("TreeBucket", TreeBucket::new);

        // Optional: Resizable map
        benchmarkResizable("ResizableConcurrentMap (Bucket)", Bucket::new);

        // Standard maps
        benchmarkStandard("ConcurrentHashMap", new ConcurrentHashMap<Integer, Integer>());
        benchmarkStandard("SynchronizedMap", Collections.synchronizedMap(new HashMap<Integer, Integer>()));
    }

    // Benchmark ConcurrentCustomMap with a given bucket type
    private void benchmarkCustom(String name, Supplier<? extends BucketInterface<Integer, Integer>> supplier)
            throws InterruptedException {
        System.gc();
        Thread.sleep(50);
        long memBefore = usedMemory();

        ConcurrentCustomMap<Integer, Integer> map = new ConcurrentCustomMap<>(16, supplier);
        BenchmarkResult result = runBenchmark(map);
        long memAfter = usedMemory();

        printResults(name, result, memAfter - memBefore);

        // visualize bucket load distribution
        try {
            int[] bucketLoads = map.getBucketLoadDistribution();
            if (bucketLoads != null)
                visualizeLoad(bucketLoads);
        } catch (Exception ignored) {}
    }

    // Benchmark ResizableConcurrentMap
    private void benchmarkResizable(String name, Supplier<? extends BucketInterface<Integer, Integer>> supplier)
            throws InterruptedException {
        System.gc();
        Thread.sleep(50);
        long memBefore = usedMemory();

        ResizableConcurrentMap<Integer, Integer> map = new ResizableConcurrentMap<>(16, 0.75, 2, supplier);
        BenchmarkResult result = runBenchmark(map.getInternalMap());
        long memAfter = usedMemory();

        printResults(name, result, memAfter - memBefore);

        // visualize underlying map
        try {
            int[] bucketLoads = map.getInternalMap().getBucketLoadDistribution();
            if (bucketLoads != null)
                visualizeLoad(bucketLoads);
        } catch (Exception ignored) {}
    }

    // Benchmark for standard maps
    private void benchmarkStandard(String name, Map<Integer, Integer> map) throws InterruptedException {
        System.gc();
        Thread.sleep(50);
        long memBefore = usedMemory();

        BenchmarkResult result = runBenchmark(map);
        long memAfter = usedMemory();

        printResults(name, result, memAfter - memBefore);
    }

    // Benchmark logic for ConcurrentCustomMap
    private BenchmarkResult runBenchmark(ConcurrentCustomMap<Integer, Integer> map) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicLong ops = new AtomicLong();
        long start = System.nanoTime();

        for (int t = 0; t < THREAD_COUNT; t++) {
            pool.submit(() -> {
                for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                    int key = ThreadLocalRandom.current().nextInt(OPERATIONS_PER_THREAD);
                    if (ThreadLocalRandom.current().nextDouble() < READ_RATIO) {
                        map.get(key);
                    } else {
                        map.put(key, key);
                        if (key % 1000 == 0)
                            map.remove(key);
                    }
                    ops.incrementAndGet();
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.MINUTES);

        long elapsed = System.nanoTime() - start;
        double seconds = elapsed / 1_000_000_000.0;
        double opsPerSec = ops.get() / seconds;
        double latency = elapsed / (double) ops.get();

        return new BenchmarkResult(ops.get(), elapsed, opsPerSec, latency);
    }

    // Benchmark logic for standard Java maps
    private BenchmarkResult runBenchmark(Map<Integer, Integer> map) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicLong ops = new AtomicLong();
        long start = System.nanoTime();

        for (int t = 0; t < THREAD_COUNT; t++) {
            pool.submit(() -> {
                for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                    int key = ThreadLocalRandom.current().nextInt(OPERATIONS_PER_THREAD);
                    if (ThreadLocalRandom.current().nextDouble() < READ_RATIO) {
                        map.get(key);
                    } else {
                        map.put(key, key);
                        if (key % 1000 == 0)
                            map.remove(key);
                    }
                    ops.incrementAndGet();
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.MINUTES);

        long elapsed = System.nanoTime() - start;
        double seconds = elapsed / 1_000_000_000.0;
        double opsPerSec = ops.get() / seconds;
        double latency = elapsed / (double) ops.get();

        return new BenchmarkResult(ops.get(), elapsed, opsPerSec, latency);
    }

    private long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private void printResults(String name, BenchmarkResult result, long memoryDelta) {
        System.out.printf("    %s%n", name);
        System.out.printf("   • Total time:     %,d ns%n", result.totalTimeNs);
        System.out.printf("   • Ops/sec:        %, .2f%n", result.opsPerSec);
        System.out.printf("   • Avg latency:    %, .2f ns/op%n", result.avgLatency);
        System.out.printf("   • Total ops:      %,d%n", result.totalOps);
        System.out.printf("   • Memory change:  %,d bytes%n", memoryDelta);
        System.out.println("----------------------------------------------------\n");
    }

    private void visualizeLoad(int[] buckets) {
        System.out.println("   Bucket Load Distribution:");
        int maxLoad = Arrays.stream(buckets).max().orElse(1);
        int maxBarLength = 50;

        for (int i = 0; i < buckets.length; i++) {
            int load = buckets[i];
            int barLength = (int) Math.round(((double) load / maxLoad) * maxBarLength);
            String bar = "#".repeat(Math.max(0, barLength));
            System.out.printf("   [%02d] %s (%d)%n", i, bar, load);
        }
        System.out.println();
    }
}
