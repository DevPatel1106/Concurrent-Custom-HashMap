package concurrentmap;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

// import static org.junit.jupiter.api.Assertions.*;

public class BenchmarkTest {
    
    private static final int THREAD_COUNT = 8;
    private static final int OPERATIONS_PER_THREAD = 100_000;
    private static final double READ_RATIO = 0.8; // 80% reads - 20% writes

    // Simple struct-like helper class
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
        System.out.printf("Threads: %d | Operations: %,d | Read Ratio: %.0f%%%n%n", THREAD_COUNT, OPERATIONS_PER_THREAD, READ_RATIO * 100);

        ConcurrentCustomMap<Integer, Integer> customMap = new ConcurrentCustomMap<>();
        Map<Integer, Integer> concurrentMap = new ConcurrentHashMap<>();
        Map<Integer, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());

        benchmarkCustom("ConcurrentCustomMap", customMap);
        benchmarkStandard("ConcurrentHashMap", concurrentMap);
        benchmarkStandard("SynchronizedMap", syncMap);
    }

    // Benchmark for custom map
    private void benchmarkCustom(String name, ConcurrentCustomMap<Integer, Integer> map) throws InterruptedException {
        System.gc(); Thread.sleep(100);
        long memBefore = usedMemory();

        BenchmarkResult result = runBenchmark(map);
        long memAfter = usedMemory();

        printResults(name, result, memAfter - memBefore);

        // Optional: visualize internal structure
        try {
            int[] bucketLoads = map.getBucketLoadDistribution(); // optional diagnostic method
            if (bucketLoads != null) visualizeLoad(bucketLoads);
        } catch (Exception ignored) {}
    }

    // Benchmark for standard Java Maps
    private void benchmarkStandard(String name, Map<Integer, Integer> map) throws InterruptedException {
        System.gc(); Thread.sleep(100);
        long memBefore = usedMemory();

        BenchmarkResult result = runBenchmark(map);
        long memAfter = usedMemory();

        printResults(name, result, memAfter - memBefore);
    }

    // Core benchmark logic for standard Maps
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
                        if (key % 1000 == 0) map.remove(key);
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

    // Core benchmark logic for Custom map
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
                        if (key % 1000 == 0) map.remove(key);
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

    // Utility to measure used memory
    private long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    // Utility to print results neatly
    private void printResults(String name, BenchmarkResult result, long memoryDelta) {
        System.out.printf("    %s%n", name);
        System.out.printf("   • Total time:     %,d ns%n", result.totalTimeNs);
        System.out.printf("   • Ops/sec:        %, .2f%n", result.opsPerSec);
        System.out.printf("   • Avg latency:    %, .2f ns/op%n", result.avgLatency);
        System.out.printf("   • Total ops:      %,d%n", result.totalOps);
        System.out.printf("   • Memory change:  %,d bytes%n", memoryDelta);
        System.out.println("----------------------------------------------------\n");
    }

    // visualize load factor (for custom map)
    private void visualizeLoad(int[] buckets) {
    System.out.println("   Bucket Load Distribution:");

    // Find the maximum load to scale the bars
    int maxLoad = Arrays.stream(buckets).max().orElse(1);
    int maxBarLength = 50; // max number of # symbols to display

    for (int i = 0; i < buckets.length; i++) {
        int load = buckets[i];

        // Scale the bar length proportionally to maxLoad
        int barLength = (int) Math.round(((double) load / maxLoad) * maxBarLength);
        String bar = "#".repeat(Math.max(0, barLength));

        System.out.printf("   [%02d] %s (%d)%n", i, bar, load);
    }
    System.out.println();
}


}

