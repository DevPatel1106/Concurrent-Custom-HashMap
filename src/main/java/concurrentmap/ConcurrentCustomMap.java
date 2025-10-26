package concurrentmap;

import utils.HashUtils;
import java.util.List;
import java.util.function.Supplier;

/**
 * A thread-safe custom HashMap supporting modular bucket implementations.
 * Delegates all operations to BucketInterface<K,V>.
 *
 * @param <K> key type
 * @param <V> value type
 */
public class ConcurrentCustomMap<K, V> {

    private final BucketInterface<K, V>[] buckets;
    private final int numBuckets;
    private final Supplier<? extends BucketInterface<K, V>> bucketSupplier;

    /**
     * Constructs a map with the given number of buckets using the provided bucket factory.
     *
     * @param numBuckets     number of buckets
     * @param bucketSupplier factory to create bucket instances
     */
    @SuppressWarnings("unchecked")
    public ConcurrentCustomMap(int numBuckets, Supplier<? extends BucketInterface<K, V>> bucketSupplier) {
        this.numBuckets = numBuckets;
        this.bucketSupplier = bucketSupplier;
        this.buckets = new BucketInterface[numBuckets];
        for (int i = 0; i < numBuckets; i++) {
            buckets[i] = bucketSupplier.get();
        }
    }

    /**
     * Computes the bucket index for a given key.
     */
    private int getBucketIndex(K key) {
        return HashUtils.getBucketIndex(key, numBuckets);
    }

    /**
     * Inserts or updates a key-value pair.
     */
    public void put(K key, V value) {
        int index = getBucketIndex(key);
        buckets[index].put(key, value);
    }

    /**
     * Retrieves a value by key.
     */
    public V get(K key) {
        int index = getBucketIndex(key);
        return buckets[index].get(key);
    }

    /**
     * Removes a key-value pair.
     */
    public V remove(K key) {
        int index = getBucketIndex(key);
        return buckets[index].remove(key);
    }

    /**
     * Returns the number of buckets.
     */
    public int getCapacity() {
        return numBuckets;
    }

    /**
     * Returns the current load (number of entries) per bucket.
     * Useful for benchmarking or visualization.
     */
    public int[] getBucketLoadDistribution() {
        int[] loads = new int[numBuckets];
        for (int i = 0; i < numBuckets; i++) {
            loads[i] = buckets[i].size();
        }
        return loads;
    }

    /**
     * Returns all entries in the map as a list.
     * Useful for rehashing during resize.
     */
    public List<Entry<K, V>> entrySet() {
        List<Entry<K, V>> entries = new java.util.ArrayList<>();
        for (BucketInterface<K, V> bucket : buckets) {
            entries.addAll(bucket.getEntries());
        }
        return entries;
    }

    /**
     * Returns the supplier used to create new buckets.
     */
    public Supplier<? extends BucketInterface<K, V>> getBucketType() {
        return bucketSupplier;
    }
}
