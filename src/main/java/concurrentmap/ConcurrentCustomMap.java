package concurrentmap;

import utils.HashUtils;

/**
 * A thread-safe custom HashMap implementation using bucket-level locking.
 * Provides fine-grained concurrency through separate locks for each bucket.
 *
 * @param <K> Type of key
 * @param <V> Type of value
 */
public class ConcurrentCustomMap<K, V> {

    private static final int NUM_BUCKETS = 16; // Default number of buckets
    private final Bucket<K, V>[] buckets; // Array of buckets

    /**
     * Constructs a concurrent custom map with default number of buckets.
     */
    @SuppressWarnings("unchecked")
    public ConcurrentCustomMap() {
        buckets = new Bucket[NUM_BUCKETS];
        for (int i = 0; i < NUM_BUCKETS; i++) {
            buckets[i] = new Bucket<>();
        }
    }

    /**
     * Computes the bucket index for a given key.
     * Uses the HashUtils utility for consistent hashing.
     *
     * @param key the key to hash
     * @return index of the corresponding bucket
     */
    private int getBucketIndex(K key) {
        return HashUtils.getBucketIndex(key, NUM_BUCKETS);
    }

    /**
     * Inserts or updates a key-value pair in the map.
     * Thread-safe due to bucket-level locking.
     *
     * @param key   the key to insert
     * @param value the value to associate
     */
    public void put(K key, V value) {
        int index = getBucketIndex(key);
        buckets[index].put(key, value);
    }

    /**
     * Retrieves a value for the given key.
     * Thread-safe due to bucket-level locking.
     *
     * @param key the key to retrieve
     * @return the associated value, or null if not found
     */
    public V get(K key) {
        int index = getBucketIndex(key);
        return buckets[index].get(key);
    }

    /**
     * Removes a key-value pair from the map.
     * Thread-safe due to bucket-level locking.
     *
     * @param key the key to remove
     * @return the removed value, or null if not found
     */
    public V remove(K key) {
        int index = getBucketIndex(key);
        return buckets[index].remove(key);
    }

    /**
     * Returns the number of buckets currently in use.
     *
     * @return total number of buckets
     */
    public int buckectCount() {
        return NUM_BUCKETS;
    }

    /**
     * Returns an array with the current load (number of entries) in each bucket.
     * Used only for benchmarking or visualization.
     *
     * @return int array of bucket loads
     */
    public int[] getBucketLoadDistribution() {
        int[] loads = new int[NUM_BUCKETS];
        for (int i = 0; i < NUM_BUCKETS; i++) {
            loads[i] = buckets[i].size(); // size() should be implemented in Bucket
        }
        return loads;
    }

}
