package uk.gov.ons.ctp.response.collection.exercise.lib.common.error;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.stream.Collectors;
import net.sourceforge.cobertura.CoverageIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/** Rest Exception Handler */
@CoverageIgnore
@ControllerAdvice
public class RestExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

  public static final String INVALID_JSON = "Provided json fails validation.";
  public static final String PROVIDED_JSON_INCORRECT = "Provided json is incorrect.";
  public static final String PROVIDED_XML_INCORRECT = "Provided xml is incorrect.";

  private static final String XML_ERROR_MESSAGE = "Could not unmarshal to";

  /**
   * CTPException Handler
   *
   * @param exception CTPException
   * @return ResponseEntity containing exception and associated HttpStatus
   */
  @ExceptionHandler(CTPException.class)
  public ResponseEntity<?> handleCTPException(CTPException exception) {
    log.error(
        "Uncaught CTPException",
        kv("exception_message", exception.getMessage()),
        kv("fault", exception.getFault()),
        exception);

    HttpStatus status;
    switch (exception.getFault()) {
      case RESOURCE_NOT_FOUND:
        status = HttpStatus.NOT_FOUND;
        break;
      case RESOURCE_VERSION_CONFLICT:
        status = HttpStatus.CONFLICT;
        break;
      case ACCESS_DENIED:
        status = HttpStatus.UNAUTHORIZED;
        break;
      case BAD_REQUEST:
      case VALIDATION_FAILED:
        status = HttpStatus.BAD_REQUEST;
        break;
      case SYSTEM_ERROR:
        status = HttpStatus.INTERNAL_SERVER_ERROR;
        break;
      default:
        status = HttpStatus.I_AM_A_TEAPOT;
        break;
    }

    return new ResponseEntity<>(exception, status);
  }

  /**
   * Handler for Invalid Request Exceptions
   *
   * @param t Throwable
   * @return ResponseEntity containing CTP Exception
   */
  @ExceptionHandler(Throwable.class)
  public ResponseEntity<?> handleGeneralException(Throwable t) {
    log.error("Uncaught Throwable", t);
    return new ResponseEntity<>(
        new CTPException(CTPException.Fault.SYSTEM_ERROR, t, t.getMessage()),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handler for Invalid Request Exceptions
   *
   * @return ResponseEntity containing CTPException
   */
  @ResponseBody
  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<?> handleInvalidRequestException(InvalidRequestException ex) {

    String errors =
        ex.getErrors()
            .getFieldErrors()
            .stream()
            .map(e -> String.format("field=%s message=%s", e.getField(), e.getDefaultMessage()))
            .collect(Collectors.joining(","));

    log.error(
        "Unhandled InvalidRequestException",
        kv("validation_errors", errors),
        kv("source_message", ex.getSourceMessage()),
        ex);
    CTPException ourException =
        new CTPException(CTPException.Fault.VALIDATION_FAILED, INVALID_JSON);
    return new ResponseEntity<>(ourException, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles Http Message Not Readable Exception
   *
   * @param ex exception
   * @return ResponseEntity containing exception and BAD_REQUEST http status
   */
  @ResponseBody
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<?> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {
    log.error("Uncaught HttpMessageNotReadableException", ex);
    String message =
        ex.getMessage().startsWith(XML_ERROR_MESSAGE)
            ? PROVIDED_XML_INCORRECT
            : PROVIDED_JSON_INCORRECT;

    CTPException ourException = new CTPException(CTPException.Fault.VALIDATION_FAILED, message);

    return new ResponseEntity<>(ourException, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles Method Argument not valid Exception
   *
   * @param ex exception
   * @return ResponseEntity containing exception and BAD_REQUEST http status
   */
  @ResponseBody
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    log.error(
        "Uncaught MethodArgumentNotValidException",
        kv("parameter", ex.getParameter().getParameterName()),
        ex);
    CTPException ourException =
        new CTPException(CTPException.Fault.VALIDATION_FAILED, INVALID_JSON);
    return new ResponseEntity<>(ourException, HttpStatus.BAD_REQUEST);
  }
}
