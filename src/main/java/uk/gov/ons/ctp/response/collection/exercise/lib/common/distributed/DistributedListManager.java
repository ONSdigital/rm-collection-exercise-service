package uk.gov.ons.ctp.response.collection.exercise.lib.common.distributed;

import java.util.List;

/**
 * Genericized Interface for apps to code against for distributed lists of lists. This manager
 * allows for the distributed storage/retrieval of arbitrary data of type T into a list of lists,
 * which like a map are keyed. Effectively :
 *
 * <p>Map&lt;String, List&lt;T&gt;&gt;
 *
 * <p>where the manager will allow the caller to retrieve either just its own keyed list or a super
 * list representing the content of all other application instances using the same configured
 * manager.
 *
 * @param <T>; The List&lt;type&gt; to be stored
 */
public interface DistributedListManager<T> {

  /**
   * Store the list against the given key in the distributed store NOTE: this thread must have
   * locked the list container either directly or thru the findList()
   *
   * @param listKey the key to store this instances list against
   * @param unlock should we unlock the container afterwards?
   * @param list the list to store
   * @throws LockingException locking exception
   */
  void saveList(String listKey, List<T> list, boolean unlock) throws LockingException;

  /**
   * get the list associated with the given key that was stored by any application instance ie the
   * list returned is the flattened, 'super' list of lists for key so if 3 instances of the same
   * app, using an identically configured list manager, store individually :
   *
   * <p>instance 1 store "keyA" : [1,2,3] instance 2 store "keyA" : [4,5,6] instance 3 store "keyA"
   * : [7,8,9]
   *
   * <p>the returned list for "keyA" will be [1,2,3,4,5,6,7,8,9]
   *
   * @param listKey the key
   * @param unlock should we unlock the container afterwards?
   * @return the super list for the key
   * @throws LockingException could not lock list
   */
  List<T> findList(String listKey, boolean unlock) throws LockingException;

  /**
   * Remove from the store the instance list stored by key
   *
   * @param listKey the key
   * @param unlock should we unlock the container afterwards?
   * @throws LockingException could not lock list
   */
  void deleteList(String listKey, boolean unlock) throws LockingException;

  /**
   * Release the lock this instance has on the list container
   *
   * @throws LockingException did we ever have the lock?
   */
  void unlockContainer() throws LockingException;

  /**
   * Try and lock the container
   *
   * @throws LockingException we failed
   */
  void lockContainer() throws LockingException;

  /**
   * Determines if the current thread has locked the list container
   *
   * @return if container is locked by current thread
   */
  boolean containerIsLockedByCurrentThread();
}
