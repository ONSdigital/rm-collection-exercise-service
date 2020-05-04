package uk.gov.ons.ctp.response.collection.exercise.lib.common.distributed;

/** Manage a count in a distributed service. */
public interface DistributedAtomicLong {

  /**
   * Get value.
   *
   * @param key the reference for the distributed AtomicLong.
   * @return return the current value.
   */
  long getValue(String key);

  /**
   * Atomically set the given value.
   *
   * @param key the reference for the distributed AtomicLong.
   * @param setValue the value to set
   */
  void setValue(String key, long setValue);

  /**
   * Atomically Increment the value.
   *
   * @param key the reference for the distributed AtomicLong.
   * @return the current value.
   */
  long incrementAndGet(String key);

  /**
   * Atomically Decrement the value.
   *
   * @param key the reference for the distributed AtomicLong.
   * @return the current value.
   */
  long decrementAndGet(String key);

  /**
   * Delete the distributed AtomicLong.
   *
   * @param key the reference for the distributed AtomicLong.
   * @return boolean true if exists and deleted, else false.
   */
  boolean delete(String key);
}
