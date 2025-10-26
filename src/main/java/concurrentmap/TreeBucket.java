package concurrentmap;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * TreeBucket provides a tree-based bucket implementation backed by a
 * {@link TreeMap}.
 * <p>
 * This structure improves performance for high-collision buckets by replacing
 * linear linked-list scans with logarithmic lookups. Thread safety is ensured
 * using a {@link ReentrantReadWriteLock}, allowing concurrent reads and
 * exclusive writes.
 *
 * @param <K> type of key (must be {@link Comparable})
 * @param <V> type of value
 */
public class TreeBucket<K extends Comparable<K>, V> implements BucketInterface<K, V> {

    /** Internal tree map for ordered key-value storage. */
    private final TreeMap<K, V> tree;

    /** Read-write lock for concurrent access control. */
    private final ReentrantReadWriteLock rwLock;

    /**
     * Constructs an empty TreeBucket.
     */
    public TreeBucket() {
        this.tree = new TreeMap<>();
        this.rwLock = new ReentrantReadWriteLock();
    }

    /**
     * Inserts or updates a key-value pair.
     * Allows multiple readers but only one writer.
     *
     * @param key   the key to insert or update
     * @param value the value to associate with the key
     * @return the previous value if key existed, otherwise null
     */
    @Override
    public V put(K key, V value) {
        rwLock.writeLock().lock();
        try {
            return tree.put(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Retrieves a value by key.
     * Uses shared read lock to allow concurrent lookups.
     *
     * @param key the key to retrieve
     * @return the associated value, or null if not found
     */
    @Override
    public V get(K key) {
        rwLock.readLock().lock();
        try {
            return tree.get(key);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Removes a key-value pair from the tree.
     * Requires exclusive write access.
     *
     * @param key the key to remove
     * @return the removed value, or null if not present
     */
    @Override
    public V remove(K key) {
        rwLock.writeLock().lock();
        try {
            return tree.remove(key);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Returns the number of entries currently in this bucket.
     * Thread-safe under concurrent access.
     *
     * @return number of entries
     */
    @Override
    public int size() {
        rwLock.readLock().lock();
        try {
            return tree.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Returns a snapshot list of all entries in this bucket.
     * Safe for concurrent use and useful for benchmarking or visualization.
     *
     * @return list of {@link Entry} objects representing key-value pairs
     */
    @Override
    public List<Entry<K, V>> getEntries() {
        rwLock.readLock().lock();
        try {
            List<Entry<K, V>> entries = new ArrayList<>();
            for (Map.Entry<K, V> e : tree.entrySet()) {
                entries.add(new Entry<>(e.getKey(), e.getValue()));
            }
            return entries;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Returns the current load (number of entries) in the bucket.
     * Alias for {@link #size()}.
     *
     * @return number of entries in the tree
     */
    public int getLoad() {
        return size();
    }

}