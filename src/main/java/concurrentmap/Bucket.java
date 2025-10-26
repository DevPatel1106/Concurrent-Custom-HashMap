package concurrentmap;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a single bucket in the Concurrent Custom HashMap.
 * Each bucket maintains its own lock for fine-grained concurrency control.
 * 
 * @param <K> Type of key
 * @param <V> Type of value
 */

public class Bucket<K, V> {
    private Entry<K, V> head; // HEad of linked list
    private final ReentrantLock lock; // Lock for thread safe operations

    /**
     * Constructs a new empty Bucket with its own lock.
     */
    public Bucket() {
        this.head = null;
        this.lock = new ReentrantLock();
    }

    /**
     * Inserts or updates a key-value pair in this bucket.
     * Uses bucket-level locking for thread safety.
     *
     * @param key   the key to insert or update
     * @param value the value to associate with the key
     */
    public void put(K key, V value) {
        lock.lock();
        try {
            // If the bucket is empty, insert at head
            if (head == null) {
                head = new Entry<>(key, value);
                return;
            }

            // Traverse the linked list to find if key already exists
            Entry<K, V> current = head;
            while (current != null) {
                if (current.keyEquals(key)) {
                    // Key exists, update value
                    current.value = value;
                    return;
                }
                if (current.next == null)
                    break; // Reached end of list
                current = current.next;
            }

            // Key not found, insert new entry at the end
            current.next = new Entry<>(key, value);

        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves a value by key from this bucket.
     * Thread-safe due to lock during traversal.
     *
     * @param key the key to look up
     * @return the value if found, otherwise null
     */
    public V get(K key) {
        lock.lock();
        try {
            Entry<K, V> current = head;
            while (current != null) {
                if (current.keyEquals(key)) {
                    return current.value;
                }
                current = current.next;
            }
            return null; // Key not found
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes a key-value pair from this bucket.
     * Thread-safe due to bucket-level lock.
     *
     * @param key the key to remove
     * @return the removed value, or null if key not found
     */
    public V remove(K key) {
        lock.lock();
        try {
            if (head == null) {
                return null; // Bucket is empty
            }

            // If head matches the key
            if (head.keyEquals(key)) {
                V oldValue = head.value;
                head = head.next; // remove first node
                return oldValue;
            }

            // Traverse list to find and unlink matching entry
            Entry<K, V> prev = head;
            Entry<K, V> current = head.next;

            while (current != null) {
                if (current.keyEquals(key)) {
                    V oldValue = current.value;
                    prev.next = current.next;
                    return oldValue;
                }
                prev = current;
                current = current.next;
            }
            return null; // Key not found
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the number of key-value entries currently stored in this bucket.
     * <p>
     * This method acquires the bucket's lock to ensure a consistent count
     * when accessed concurrently by multiple threads.
     *
     * @return the total number of entries in this bucket
     */
    public int size() {
        lock.lock();
        try {
            int count = 0;
            Entry<K, V> current = head;
            while (current != null) {
                count++;
                current = current.next;
            }
            return count;
        } finally {
            lock.unlock();
        }
    }

}
