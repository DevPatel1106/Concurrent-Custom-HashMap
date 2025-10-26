package concurrentmap;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.*;

/**
 * Unit tests for ConcurrentCustomMap
 * - Single-threaded correctness
 * - Multi-threaded thread safety
 */
class ConcurrentCustomMapTest {

    @Test
    void singleThreadPutGetRemove() {
        ConcurrentCustomMap<String, Integer> map = new ConcurrentCustomMap<>();

        // Put entries
        map.put("one", 1);
        map.put("two", 2);

        // Get entries
        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
        assertNull(map.get("three")); // key not present

        // Remove entries
        assertEquals(1, map.remove("one"));
        assertNull(map.get("one"));
    }

    @Test
    void multiThreadedPutAndGet() throws InterruptedException {
        ConcurrentCustomMap<Integer, Integer> map = new ConcurrentCustomMap<>();
        int threadCount = 10;
        int operationsPerThread = 1000;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //Multiple threads perform put() operations
        for(int i=0; i<threadCount; i++) {
            int threadId = i;
            executor.submit(() -> {
                for(int j=0; j<operationsPerThread; j++) {
                    map.put(threadId*operationsPerThread+j,j);
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        //check total entries
        int expectedSize = threadCount*operationsPerThread;
        int actualCount =0;
        for(int i=0; i<expectedSize; i++) {
            if(map.get(i)!=null) actualCount++;
        }
        assertEquals(expectedSize, actualCount, "All entries should be present after concurrent put operations.");
    }

    @Test
    void concurrentReadWriteRemove() throws InterruptedException {
        ConcurrentCustomMap<Integer, Integer> map = new ConcurrentCustomMap<>();
        int threadCount = 8;
        int operations = 500;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int t=0; t<threadCount; t++) {
            int threadId = t;
            executor.submit(() -> {
                for(int i=0; i<operations; i++) {
                    map.put(i,i);
                    map.get(i);
                    map.remove(i);
                }
                latch.countDown();
            });
        }
        latch.await();
        executor.shutdown();

        //After all threads remove keys, map should be empty
        int nonNullCount =0;
        for(int i=0; i<operations; i++) {
            if(map.get(i) != null) nonNullCount++;
        }
        assertEquals(0, nonNullCount, "Mapshould be empty after concurrent removals.");
    }
}

