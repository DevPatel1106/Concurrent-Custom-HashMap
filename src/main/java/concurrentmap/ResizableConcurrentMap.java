package concurrentmap;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Resizable wrapper for ConcurrentCustomMap.
 * Monitors average bucket load and resizes automatically.
 *
 * @param <K> key type
 * @param <V> value type
 */
public class ResizableConcurrentMap<K, V> {

    private final AtomicReference<ConcurrentCustomMap<K, V>> mapRef;
    private final ReentrantLock resizeLock = new ReentrantLock();
    private final double loadFactorThreshold;
    private final int resizeMultiplier;
    private final Supplier<? extends BucketInterface<K, V>> bucketSupplier;

    /**
     * Default constructor using lock-based Bucket.
     */
    public ResizableConcurrentMap() {
        this(16, 0.75, 2, Bucket::new);
    }

    /**
     * Constructor with custom parameters.
     *
     * @param initialCapacity     initial number of buckets
     * @param loadFactorThreshold threshold to trigger resize
     * @param resizeMultiplier    how much to grow on resize
     * @param bucketSupplier      supplier to create bucket instances
     */
    public ResizableConcurrentMap(int initialCapacity,
                                  double loadFactorThreshold,
                                  int resizeMultiplier,
                                  Supplier<? extends BucketInterface<K, V>> bucketSupplier) {
        this.bucketSupplier = bucketSupplier;
        this.mapRef = new AtomicReference<>(new ConcurrentCustomMap<>(initialCapacity, bucketSupplier));
        this.loadFactorThreshold = loadFactorThreshold;
        this.resizeMultiplier = resizeMultiplier;
    }

    public void put(K key, V value) {
        mapRef.get().put(key, value);
        checkResize();
    }

    public V get(K key) {
        return mapRef.get().get(key);
    }

    public V remove(K key) {
        return mapRef.get().remove(key);
    }

    public int size() {
        int total = 0;
        for (int load : mapRef.get().getBucketLoadDistribution()) {
            total += load;
        }
        return total;
    }

    private void checkResize() {
        ConcurrentCustomMap<K, V> current = mapRef.get();
        double avgLoad = (double) size() / current.getCapacity();

        if (avgLoad > loadFactorThreshold && resizeLock.tryLock()) {
            try {
                if ((double) size() / current.getCapacity() > loadFactorThreshold) {
                    resize(current);
                }
            } finally {
                resizeLock.unlock();
            }
        }
    }

    private void resize(ConcurrentCustomMap<K, V> oldMap) {
        int newCapacity = oldMap.getCapacity() * resizeMultiplier;
        ConcurrentCustomMap<K, V> newMap = new ConcurrentCustomMap<>(newCapacity, bucketSupplier);

        for (Entry<K, V> entry : oldMap.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue());
        }

        mapRef.set(newMap); // atomic swap
    }

    /**
     * Returns the underlying map (for debugging or benchmarking).
     */
    public ConcurrentCustomMap<K, V> getInternalMap() {
        return mapRef.get();
    }
}
