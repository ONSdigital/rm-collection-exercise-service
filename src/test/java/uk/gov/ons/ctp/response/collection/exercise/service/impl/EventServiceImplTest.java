package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.when;

/**
 * Class containing tests for EventServiceImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class EventServiceImplTest {
    @Mock
    private EventRepository eventRepository;

    @Mock
    private CollectionExerciseService collectionExerciseService;

    @InjectMocks
    private EventServiceImpl eventService;

    /**
     * Given collection excercise does not exist
     * When event is created
     * Then exception is thrown
     */
    @Test
    public void givenCollectionExcerciseDoesNotExistWhenEventIsCreatedThenExceptionIsThrown() {
        EventDTO eventDto = new EventDTO();
        UUID collexUuid = UUID.randomUUID();
        eventDto.setCollectionExerciseId(collexUuid);

        try {
            eventService.createEvent(eventDto);

            fail("Created event with non-existent collection exercise");
        } catch (CTPException e) {
            // Expected 404
            assertEquals(CTPException.Fault.RESOURCE_NOT_FOUND, e.getFault());
        }
    }

    /**
     * Given event already exists
     * When event is created
     * Then exception is thrown
     */
    @Test
    public void givenEventAlreadyExistsWhenEventIsCreatedThenExceptionIsThrown() {
        String tag = EventService.Tag.mps.name();
        EventDTO eventDto = new EventDTO();
        CollectionExercise collex = new CollectionExercise();
        UUID collexUuid = UUID.randomUUID();
        eventDto.setCollectionExerciseId(collexUuid);
        eventDto.setTag(tag);
        collex.setId(collexUuid);

        when(collectionExerciseService.findCollectionExercise(collexUuid)).thenReturn(collex);
        when(eventRepository.findOneByCollectionExerciseAndTag(collex, tag)).thenReturn(new Event());

        try {
            eventService.createEvent(eventDto);

            fail("Created event with non-existent collection exercise");
        } catch (CTPException e) {
            // Expected 409
            assertEquals(CTPException.Fault.RESOURCE_VERSION_CONFLICT, e.getFault());
        }
    }
}
