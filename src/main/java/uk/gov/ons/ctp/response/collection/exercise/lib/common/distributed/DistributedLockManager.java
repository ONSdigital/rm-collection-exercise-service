package uk.gov.ons.ctp.response.collection.exercise.lib.common.distributed;

/**
 * As the name implies this is a manager only for distributed locks. Not the lock itself. An
 * implementation is meant to hide the actual implementing technology from the client code, so that
 * if need be we can easily swap out one implementation specific implementation for another.
 */
public interface DistributedLockManager {
  /**
   * for the given named lock, lock it.
   *
   * @param key the name of the lock
   * @return true if locked
   */
  boolean lock(String key);

  /**
   * acquires the lock if it is free within the given waiting time and the current thread has not
   * been interrupted
   *
   * @param key the name of the lock
   * @param time the maximum time to wait for the lock
   * @return true if locked
   * @throws InterruptedException if current thread is interrupted acquiring lock
   */
  boolean trylock(String key, long time) throws InterruptedException;

  /**
   * unlock the given named lock, if indeed the lock manager originally locked it, and the calling
   * thread is the thread that created the lock in the first place..
   *
   * @param key the lock name
   */
  void unlock(String key);

  /**
   * find if the named lock is locked
   *
   * @param key the name of the lock
   * @return true if the lock exists and was created by this manager. The lock may not have been
   *     created by the calling thread.
   */
  boolean isLocked(String key);

  /** force unlock of the distributed locks created by this DistributedLockManager instance */
  void unlockInstanceLocks();
}
