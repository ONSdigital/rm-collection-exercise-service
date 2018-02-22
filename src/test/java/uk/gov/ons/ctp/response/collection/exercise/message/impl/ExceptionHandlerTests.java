package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import org.junit.Before;
import org.junit.Test;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.GenericMessage;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.CollectionInstrumentMessageDTO;

import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for ExceptionHandler class
 */
public class ExceptionHandlerTests {

    private static final UUID COLLECTION_INSTRUMENT_ID = UUID.fromString("699632ec-c75b-4454-a0ae-d15c592a6473");
    private static final UUID COLLECTION_EXERCISE_ID = UUID.fromString("d42f358c-812c-45f7-a064-39739d0189a6");

    private static final CTPException.Fault EXCEPTION_FAULT = CTPException.Fault.RESOURCE_VERSION_CONFLICT;
    private static final String EXCEPTION_MESSAGE = "Test exception message";

    private ExceptionHandler exceptionHandler;

    private CollectionInstrumentMessageDTO messageDto;

    private CTPException ctpException;

    /**
     * Create objects common to many tests
     */
    @Before
    public void setUp(){
        this.exceptionHandler = new ExceptionHandler();
        this.ctpException = new CTPException(EXCEPTION_FAULT, EXCEPTION_MESSAGE);
        this.messageDto = new CollectionInstrumentMessageDTO(null, COLLECTION_EXERCISE_ID.toString(), COLLECTION_INSTRUMENT_ID.toString());
    }

    /**
     * Given null exception
     * When handleException
     * Then empty response
     */
    @Test
    public void givenNullExceptionWhenHandleExceptionThenEmptyResponse(){
        Map<ExceptionHandler.ResultKey,String> result = this.exceptionHandler.handleException(null);

        assertEquals(0, result.size());
    }

    /**
     * Given all fields
     * When handleException
     * Then all fields returned
     */
    @Test
    public void givenAllFieldsWhenHandleExceptionThenAllFields(){
        GenericMessage<CollectionInstrumentMessageDTO> message = new GenericMessage<>(this.messageDto);
        MessageHandlingException exception = new MessageHandlingException(message, this.ctpException);

        Map<ExceptionHandler.ResultKey, String> result = this.exceptionHandler.handleException(exception);

        assertEquals(COLLECTION_INSTRUMENT_ID.toString(), result.get(ExceptionHandler.ResultKey.collectionInstrument));
        assertEquals(COLLECTION_EXERCISE_ID.toString(), result.get(ExceptionHandler.ResultKey.collectionExercise));
        assertEquals(EXCEPTION_FAULT.name(), result.get(ExceptionHandler.ResultKey.errorType));
        assertEquals(EXCEPTION_MESSAGE, result.get(ExceptionHandler.ResultKey.errorMessage));
        assertEquals(this.ctpException.getTimestamp(), Long.parseLong(result.get(ExceptionHandler.ResultKey.errorTimestamp)));
    }

    /**
     * Given no failed message
     * When handleException
     * Then only exeception details returned
     */
    @Test
    public void givenNoFailedMessageWhenHandleExceptionThenOnlyExeceptionDetails(){
        MessageHandlingException exception = new MessageHandlingException(null, this.ctpException);

        Map<ExceptionHandler.ResultKey, String> result = this.exceptionHandler.handleException(exception);

        assertNull(result.get(ExceptionHandler.ResultKey.collectionInstrument));
        assertNull(result.get(ExceptionHandler.ResultKey.collectionExercise));
        assertEquals(EXCEPTION_FAULT.name(), result.get(ExceptionHandler.ResultKey.errorType));
        assertEquals(EXCEPTION_MESSAGE, result.get(ExceptionHandler.ResultKey.errorMessage));
        assertEquals(this.ctpException.getTimestamp(), Long.parseLong(result.get(ExceptionHandler.ResultKey.errorTimestamp)));
    }

    /**
     * Given no exception
     * When handleException
     * Then only message details returned
     */
    @Test
    public void givenNoExceptionWhenHandleExceptionThenOnlyMessageDetails(){
        GenericMessage<CollectionInstrumentMessageDTO> message = new GenericMessage<>(this.messageDto);
        MessageHandlingException exception = new MessageHandlingException(message, (Exception)null);

        Map<ExceptionHandler.ResultKey, String> result = this.exceptionHandler.handleException(exception);

        assertEquals(COLLECTION_INSTRUMENT_ID.toString(), result.get(ExceptionHandler.ResultKey.collectionInstrument));
        assertEquals(COLLECTION_EXERCISE_ID.toString(), result.get(ExceptionHandler.ResultKey.collectionExercise));
        assertNull(result.get(ExceptionHandler.ResultKey.errorType));
        assertNull(result.get(ExceptionHandler.ResultKey.errorMessage));
        assertNull(result.get(ExceptionHandler.ResultKey.errorTimestamp));
    }

    /**
     * Given non CTPException
     * When handleException
     * Then only exception message returned
     */
    @Test
    public void givenNonCtpExceptionWhenHandleExceptionThenOnlyExceptionMessage(){
        GenericMessage<CollectionInstrumentMessageDTO> message = new GenericMessage<>(this.messageDto);
        MessageHandlingException exception = new MessageHandlingException(message, new Exception(EXCEPTION_MESSAGE));

        Map<ExceptionHandler.ResultKey, String> result = this.exceptionHandler.handleException(exception);

        assertEquals(COLLECTION_INSTRUMENT_ID.toString(), result.get(ExceptionHandler.ResultKey.collectionInstrument));
        assertEquals(COLLECTION_EXERCISE_ID.toString(), result.get(ExceptionHandler.ResultKey.collectionExercise));
        assertNull(result.get(ExceptionHandler.ResultKey.errorType));
        assertNull(result.get(ExceptionHandler.ResultKey.errorTimestamp));
        assertEquals(EXCEPTION_MESSAGE, result.get(ExceptionHandler.ResultKey.errorMessage));
    }
}
