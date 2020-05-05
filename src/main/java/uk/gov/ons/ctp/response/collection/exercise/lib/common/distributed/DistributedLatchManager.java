package uk.gov.ons.ctp.response.collection.exercise.lib.common.distributed;

/** Manage a distributed CountDownLatch for synchronisation of distributed service instances. */
public interface DistributedLatchManager {

  /**
   * Set distributed countdownlatch by name. Sets count value only if latch already has reached zero
   * or is not set at all.
   *
   * @param key the name of the latch.
   * @param instanceCount value to which to set latch count.
   * @return true if count set, false if previous count existed and not reached zero.
   */
  boolean setCountDownLatch(String key, long instanceCount);

  /**
   * If the current distributed latch count is greater than zero then it is decremented. If the new
   * count is zero then all waiting threads are re-enabled for thread scheduling purposes. If the
   * current count equals zero then nothing happens.
   *
   * @param key the name of the latch to decrement.
   */
  void countDown(String key);

  /**
   * Causes the current thread to block until the latch has counted down to zero or a configurable
   * timeout is reached, unless the thread is interrupted. If the current count is zero then this
   * method returns immediately. When the count reaches zero due to invocations of the countDown()
   * method the thread becomes runnable and able to be scheduled again.
   *
   * @param key the name of the latch to block on until reaching zero.
   * @return true if the count reached zero and false if the waiting time elapsed.
   * @throws InterruptedException if thread is interrupted in blocked state.
   */
  boolean awaitCountDownLatch(String key) throws InterruptedException;

  /**
   * Deletes the distributed object representing the latch.
   *
   * @param key the name of the latch to delete.
   * @return true if distributed object exists and false otherwise.
   */
  boolean deleteCountDownLatch(String key);
}
