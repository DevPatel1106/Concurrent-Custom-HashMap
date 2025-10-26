package utils;

/**
 * Utility class for computing hash values and mapping keys to bucket indices.
 */
public final class HashUtils {
    // Private constructor to prevent instantiation
    private HashUtils() {}

    /**
     * Computes the bucket index for a given key and total number of buckets.
     * Ensures non-negative indices even for negative hash codes.
     *
     * @param key        the key whose hash is to be computed
     * @param numBuckets total number of buckets in the hash map
     * @return the computed bucket index (0 <= index < numBuckets)
     */
    public static int getBucketIndex(Object key, int numBuckets) {
        if(key == null) {
            return 0; //null keys always map to bucket 0
        }
        int hash = key.hashCode();
        
        // Handle negative hash codes by masking off the sign bit
        int positiveHash = hash & 0x7fffffff;
        return positiveHash % numBuckets;
    }
}
