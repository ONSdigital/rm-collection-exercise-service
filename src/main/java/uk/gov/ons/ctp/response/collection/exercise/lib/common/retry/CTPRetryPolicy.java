package uk.gov.ons.ctp.response.collection.exercise.lib.common.retry;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.base.Joiner;
import java.util.Collections;
import java.util.List;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.context.RetryContextSupport;
import org.springframework.util.ClassUtils;

/**
 * A RetryPolicy that will retry ONLY when the thrown exception's cause belongs to a list of
 * retryableExceptions (see the retryForException implementation).
 *
 * <p>By default, it will retry 3 times and only if the thrown exception's cause is an unchecked
 * exception.
 *
 * <p>This RetryPolicy is used in our Spring Integration flows when picking up a message off a
 * queue.
 */
@CoverageIgnore
public class CTPRetryPolicy implements RetryPolicy {

  private static final Logger log = LoggerFactory.getLogger(CTPRetryPolicy.class);

  private static final int DEFAULT_MAX_ATTEMPTS = 3;
  private static final String RUNTIME_EXCEPTION = "java.lang.RuntimeException";

  private volatile int maxAttempts;
  private volatile List<String>
      retryableExceptions; // TODO Make it a List<Class<? extends Throwable>

  /** Default CTP Retry Policy Constructor */
  public CTPRetryPolicy() {
    this(DEFAULT_MAX_ATTEMPTS);
  }

  /**
   * CTP Retry Policy Constructor
   *
   * @param maxAttempts maximum retry attempts
   */
  public CTPRetryPolicy(int maxAttempts) {
    this(maxAttempts, Collections.singletonList(RUNTIME_EXCEPTION));
  }

  /**
   * CTP Retry Policy Constructor
   *
   * @param maxAttempts maximum retry attempts
   * @param retryableExceptions exceptions to retry for
   */
  public CTPRetryPolicy(int maxAttempts, List<String> retryableExceptions) {
    this.maxAttempts = maxAttempts;
    this.retryableExceptions = retryableExceptions;
  }

  /**
   * To decide if a retrial is required.
   *
   * @param context the RetryContext
   * @return true if retrial is required
   */
  public boolean canRetry(RetryContext context) {
    Throwable lastThrowable = context.getLastThrowable();
    return (lastThrowable == null || this.retryForException(lastThrowable))
        && context.getRetryCount() < this.maxAttempts;
  }

  /**
   * Has to be there as per interface RetryPolicy
   *
   * @param status the RetryContext
   */
  public void close(RetryContext status) {}

  /**
   * Identical implementation to SimpleRetryPolicy
   *
   * @param context the RetryContext
   * @param throwable the Throwable
   */
  public void registerThrowable(RetryContext context, Throwable throwable) {
    CTPRetryContext simpleContext = (CTPRetryContext) context;
    simpleContext.registerThrowable(throwable);
  }

  /**
   * Identical implementation to SimpleRetryPolicy
   *
   * @param parent the RetryContext
   * @return the RetryContext
   */
  public RetryContext open(RetryContext parent) {
    return new CTPRetryContext(parent);
  }

  /**
   * To determine if a retry is required for the given Throwable
   *
   * @param ex the Throwable to check for retry
   * @return true if a retry is required
   */
  private boolean retryForException(Throwable ex) {
    try {
      for (String className : retryableExceptions) {
        if (Class.forName(className).isInstance(ex.getCause())) {
          return true;
        }
      }
    } catch (ClassNotFoundException e) {
      log.with("class_names", Joiner.on(",").join(retryableExceptions))
          .error("Invalid classname", e);
    }

    return false;
  }

  /**
   * Identical implementation to SimpleRetryPolicy
   *
   * @return a representation string
   */
  public String toString() {
    return ClassUtils.getShortName(this.getClass()) + "[maxAttempts=" + this.maxAttempts + "]";
  }

  /** To mirror implementation in SimpleRetryPolicy */
  private static class CTPRetryContext extends RetryContextSupport {
    /**
     * Method to mirror implementation in SimpleRetryPolicy
     *
     * @param parent the RetryContext
     */
    CTPRetryContext(RetryContext parent) {
      super(parent);
    }
  }
}
