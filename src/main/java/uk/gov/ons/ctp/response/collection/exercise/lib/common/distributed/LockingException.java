package uk.gov.ons.ctp.response.collection.exercise.lib.common.distributed;

/**
 * Used to indicate to the caller that the optimistic locking of an object in the distributed store
 * has failed - another instance has updated the object since the local manager last read it
 */
public class LockingException extends Exception {

  private static final long serialVersionUID = 7509566308215475091L;

  /** Empty Locking Exception constructor */
  public LockingException() {}

  /**
   * Locking Exception Constructor
   *
   * @param message message
   */
  public LockingException(String message) {
    super(message);
  }

  /**
   * Locking Exception Constructor
   *
   * @param cause cause of lock
   */
  public LockingException(Throwable cause) {
    super(cause);
  }

  /**
   * Locking Exception Constructor
   *
   * @param message message
   * @param cause cause of lock
   */
  public LockingException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Locking Exception Constructor
   *
   * @param message message
   * @param cause cause of lock
   * @param enableSuppression whether suppression should be enabled
   * @param writableStackTrace writable stack trace
   */
  public LockingException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
