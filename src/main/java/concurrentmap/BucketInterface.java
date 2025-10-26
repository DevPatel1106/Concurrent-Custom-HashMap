package concurrentmap;

import java.util.ArrayList;
import java.util.List;


/**
 * BucketInterface defines a common interface for all bucket types in the concurrent map.
 * This allows ConcurrentCustomMap to operate on any bucket implementation
 * (e.g., linked-list, lock-free, or tree-based) without changing map logic.
 */
public interface BucketInterface<K,V> {
    
    /**
     * Adds or updates a key-value pair in the bucket.
     * @param key the key to insert or update
     * @param value the value associated with the key
     * @return the previous value associated with the key, or null if none existed
     */
    V put(K key, V value);

    /**
     * Retrieves the value associated with the key.
     * @param key the key to look up
     * @return the value associated with the key, or null if not found
     */
    V get(K key);

    /**
     * Removes the key-value pair from the bucket.
     * @param key the key to remove
     * @return the removed value, or null if key was not present
     */
    V remove(K key);

    /**
     * Returns the number of entries in this bucket.
     * @return size of the bucket
     */
    int size();

    /**
     * Returns the number of entries in this bucket.
     * @return size of the bucket
     */
    int getLoad();

    /**
     * Optional: returns all entries in this bucket.
     * Useful for iteration or debugging.
     * @return list of entries
     */
    List<Entry<K, V>> getEntries();

}
