package concurrentmap;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Unit tests for ConcurrentCustomMap
 * - Single-threaded correctness
 * - Multi-threaded thread safety
 * - Modular bucket support (Bucket, LockFreeBucket, TreeBucket)
 */
class ConcurrentCustomMapTest {

    private <K,V> ConcurrentCustomMap<K,V> createMapWithSupplier(Supplier<? extends BucketInterface<K,V>> supplier) {
        return new ConcurrentCustomMap<>(16, supplier);
    }

    @Test
    void singleThreadPutGetRemoveDefaultBucket() {
        ConcurrentCustomMap<String, Integer> map = createMapWithSupplier(Bucket::new);

        map.put("one",1);
        map.put("two",2);

        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
        assertNull(map.get("three"));

        assertEquals(1, map.remove("one"));
        assertNull(map.get("one"));
    }

    @Test
    void singleThreadPutGetRemoveLockFreeBucket() {
        ConcurrentCustomMap<String, Integer> map = createMapWithSupplier(LockFreeBucket::new);

        map.put("one",1);
        map.put("two",2);

        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
        assertNull(map.get("three"));

        assertEquals(1, map.remove("one"));
        assertNull(map.get("one"));
    }

    @Test
    void singleThreadPutGetRemoveTreeBucket() {
        ConcurrentCustomMap<String, Integer> map = createMapWithSupplier(TreeBucket::new);

        map.put("one",1);
        map.put("two",2);

        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
        assertNull(map.get("three"));

        assertEquals(1, map.remove("one"));
        assertNull(map.get("one"));
    }

    @Test
    void multiThreadedPutAndGet() throws InterruptedException {
        ConcurrentCustomMap<Integer, Integer> map = createMapWithSupplier(Bucket::new);
        int threadCount = 10;
        int operationsPerThread = 1000;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i=0;i<threadCount;i++) {
            int threadId = i;
            executor.submit(() -> {
                for(int j=0;j<operationsPerThread;j++) {
                    map.put(threadId*operationsPerThread+j,j);
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        int expectedSize = threadCount * operationsPerThread;
        int actualCount = 0;
        for(int i=0;i<expectedSize;i++) {
            if(map.get(i) != null) actualCount++;
        }
        assertEquals(expectedSize, actualCount);
    }

    @Test
    void concurrentReadWriteRemove() throws InterruptedException {
        ConcurrentCustomMap<Integer, Integer> map = createMapWithSupplier(Bucket::new);
        int threadCount = 8;
        int operations = 500;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int t=0;t<threadCount;t++) {
            int threadId = t;
            executor.submit(() -> {
                for(int i=0;i<operations;i++) {
                    map.put(i,i);
                    map.get(i);
                    map.remove(i);
                }
                latch.countDown();
            });
        }
        latch.await();
        executor.shutdown();

        int nonNullCount = 0;
        for(int i=0;i<operations;i++) {
            if(map.get(i) != null) nonNullCount++;
        }
        assertEquals(0, nonNullCount);
    }
}

