package uk.gov.ons.ctp.response.collection.exercise.message;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandlingException;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.CollectionInstrumentMessageDTO;

/**
 * Error handling class containing service activator method to convert MessageHandlingException into
 * a map that's more friendly to posting as messages
 */
@MessageEndpoint
public class ExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

  /**
   * Service activator method that accepts a MessageHandlingException, pulls out the important bits
   * and returns them as a map suitable for posting as a Rabbit message or logging. It pulls
   * messages from the ciErrorChannel Spring integration channel and the results get posted to the
   * ciErrorCleanChannel
   *
   * @param e a message handling exception
   * @return a map containing details of the collection exercise & instument if available and the
   *     error message
   */
  @ServiceActivator(inputChannel = "ciErrorChannel", outputChannel = "ciErrorCleanChannel")
  public Map<ResultKey, String> handleException(final MessageHandlingException e) {
    Map<ResultKey, String> result = new HashMap<>();

    // Exceptions thrown in error handlers are really annoying so check all incoming data THOROUGHLY
    if (e != null) {
      if (e.getFailedMessage() != null) {
        Object payload = e.getFailedMessage().getPayload();

        if (payload != null && payload instanceof CollectionInstrumentMessageDTO) {
          CollectionInstrumentMessageDTO dto = (CollectionInstrumentMessageDTO) payload;

          result.put(ResultKey.collectionExercise, dto.getExerciseId().toString());
          result.put(ResultKey.collectionInstrument, dto.getInstrumentId().toString());
        }
      }
      Throwable t = e.getCause();

      if (t != null) {
        if (t instanceof CTPException) {
          CTPException ctpException = (CTPException) t;

          result.put(ResultKey.errorType, ctpException.getFault().name());
          result.put(ResultKey.errorTimestamp, Long.toString(ctpException.getTimestamp()));
        }

        result.put(ResultKey.errorMessage, t.getLocalizedMessage());
      }
    }

    return result;
  }

  /** Definition of fields that appear in error message sent to DLQ */
  enum ResultKey {
    collectionExercise,
    collectionInstrument,
    errorType,
    errorMessage,
    errorTimestamp
  }
}
