package concurrentmap;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * LockFreeBucket provides a non-blocking bucket implementation using
 * {@link AtomicReference} and compare-and-set (CAS) operations.
 * <p>
 * Unlike {@link Bucket}, which uses explicit locks, this bucket relies
 * on atomic primitives to guarantee thread safety without lock contention.
 * <p>
 * Suitable for high-concurrency scenarios where fine-grained locking overhead
 * becomes significant.
 *
 * @param <K> type of key
 * @param <V> type of value
 */
public class LockFreeBucket<K, V> implements BucketInterface<K, V> {

    /** Head node of the lock-free linked list. */
    private final AtomicReference<Entry<K, V>> head;

    /**
     * Constructs an empty LockFreeBucket with an atomic head reference.
     */
    public LockFreeBucket() {
        this.head = new AtomicReference<>(null);
    }

    /**
     * Inserts or updates a key-value pair using CAS-based atomic updates.
     * This operation retries until success to ensure atomicity without locks.
     *
     * @param key   the key to insert or update
     * @param value the value associated with the key
     * @return the previous value if key existed, otherwise null
     */
    @Override
    public V put(K key, V value) {
        while (true) {
            Entry<K, V> currentHead = head.get();
            Entry<K, V> current = currentHead;

            // search if the key already exists
            while (current != null) {
                if (current.keyEquals(key)) {
                    // key exisits - update in place (safe as value is volatile in Entry)
                    V oldValue = current.value;
                    current.value = value;
                    return oldValue;
                }
                current = current.next;
            }

            // Entry not found - create new node and attempt CAS
            Entry<K, V> newNode = new Entry<>(key, value);
            newNode.next = currentHead;

            if (head.compareAndSet(currentHead, newNode)) {
                // successfully insert at head
                return null;
            }

            // Retry on CAS failure
        }
    }

    /**
     * Retrieves a value by key from this bucket.
     * Lock-free traversal — no synchronization required.
     *
     * @param key the key to look up
     * @return the value if found, otherwise null
     */
    @Override
    public V get(K key) {
        Entry<K, V> current = head.get();
        while (current != null) {
            if (current.keyEquals(key)) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    /**
     * Removes a key-value pair atomically.
     * Uses retry-based CAS to ensure correctness under concurrency.
     *
     * @param key the key to remove
     * @return the removed value, or null if not found
     */
    @Override
    public V remove(K key) {
        while (true) {
            Entry<K, V> currentHead = head.get();
            Entry<K, V> current = currentHead;
            Entry<K, V> prev = null;
            V removedValue = null;

            // Build a new list excluding the target key
            Entry<K, V> newHead = null;
            Entry<K, V> newTail = null;

            while (current != null) {
                if (current.keyEquals(key)) {
                    removedValue = current.value;
                    // Skip adding this entry (effectively deleting it)
                } else {
                    Entry<K, V> copied = new Entry<>(current.key, current.value);
                    if (newHead == null) {
                        newHead = copied;
                        newTail = copied;
                    } else {
                        newTail.next = copied;
                        newTail = copied;
                    }
                }
                current = current.next;
            }

            if (head.compareAndSet(currentHead, newHead)) {
                return removedValue;
            }
            // Retry if another thread modified head concurrently
        }
    }

    /**
     * Returns the number of key-value pairs in this bucket.
     * Note: May be slightly inconsistent under concurrent writes.
     *
     * @return approximate size of this bucket
     */
    @Override
    public int size() {
        int count = 0;
        Entry<K, V> current = head.get();
        while (current != null) {
            count++;
            current = current.next;
        }
        return count;
    }

    /**
     * Returns a snapshot list of entries currently in the bucket.
     * Thread-safe but not guaranteed to reflect atomic state at a single point in time.
     *
     * @return list of {@link Entry} objects representing all key-value pairs
     */
    @Override
    public List<Entry<K, V>> getEntries() {
        List<Entry<K, V>> entries = new ArrayList<>();
        Entry<K, V> current = head.get();
        while (current != null) {
            entries.add(current);
            current = current.next;
        }
        return entries;
    }

    /**
     * Optional helper for benchmarking and visualization.
     * Alias for {@link #size()}.
     *
     * @return approximate bucket load
     */
    public int getLoad() {
        return size();
    }
}
