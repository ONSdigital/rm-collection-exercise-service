package uk.gov.ons.ctp.response.collection.exercise.lib.common.distributed;

/**
 * Manage a count of a distributed service. It is the calling code concern to ensure integrity of
 * the count.
 */
public interface DistributedInstanceManager {

  /**
   * Get count of running service instances.
   *
   * @param key the name of the service instance count.
   * @return the count of running service instances.
   */
  long getInstanceCount(String key);

  /**
   * Increment count of running service instances.
   *
   * @param key the name of the service instance count.
   * @return the count of running service instances.
   */
  long incrementInstanceCount(String key);

  /**
   * Decrement count of running service instances.
   *
   * @param key the name of the service instance count.
   * @return the count of running service instances.
   */
  long decrementInstanceCount(String key);
}
