package concurrentmap;

/**
 * Represents a single key-value pair in a bucket for the concurrent hashmap.
 * Supports linked list chaining in case of collisions.
 * @param <K> Type of key
 * @param <V> Type of value
 */

public class Entry<K, V> {
    final K key;
    volatile V value;
    Entry<K, V> next;

    /**
     * Constructs a new Entry with the given key and value.
     * @param key   the key for this entry
     * @param value the value associated with the key
     */
    public Entry(K key, V value) {
        this.key = key;
        this.value = value;
        this.next = null; // next to be set in case of collision
    }

    /**
     * Utility method to check if a given key equals this entry's key.
     * @param otherKey the key to compare
     * @return true if keys are equal
     */
    public boolean keyEquals(K otherKey) {
        return key == null ? otherKey == null : key.equals(otherKey);
    }

    /**
     * Returns the key of this entry.
     *
     * @return the key
     */
    public K getKey() {
        return key;
    }

    /**
     * Returns the value of this entry.
     *
     * @return the value
     */
    public V getValue() {
        return value;
    }

    /**
     * Sets the value of this entry.
     *
     * @param value new value
     */
    public void setValue(V value) {
        this.value = value;
    }
}
