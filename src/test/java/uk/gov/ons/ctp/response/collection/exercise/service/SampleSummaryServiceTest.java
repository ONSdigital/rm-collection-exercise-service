package uk.gov.ons.ctp.response.collection.exercise.service;

import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.message.SampleSummaryActivationPublisher;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;

@RunWith(MockitoJUnitRunner.class)
public class SampleSummaryServiceTest {

  @Mock private SampleLinkRepository sampleLinkRepository;

  @Mock private CollectionExerciseRepository collectionExerciseRepository;

  @Mock private SampleSummaryActivationPublisher sampleSummaryActivationPublisher;

  @Mock private CollectionExerciseService collectionExerciseService;

  @Mock private EventRepository eventRepository;

  @InjectMocks private SampleSummaryService sampleSummaryService;

  @Test
  public void testActivateSamples() throws Exception {
    UUID collectionExerciseId = UUID.randomUUID();
    UUID surveyId = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();

    SampleLink sampleLink = new SampleLink();
    sampleLink.setSampleSummaryId(sampleSummaryId);
    sampleLink.setCollectionExerciseId(collectionExerciseId);

    List<SampleLink> sampleLinks = new ArrayList<>();
    sampleLinks.add(sampleLink);

    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(collectionExerciseId);
    collectionExercise.setSurveyId(surveyId);

    Event event = new Event();
    event.setTimestamp(new Timestamp(System.currentTimeMillis()));

    when(collectionExerciseRepository.findOneById(collectionExerciseId))
        .thenReturn(collectionExercise);
    when(sampleLinkRepository.findByCollectionExerciseId(collectionExerciseId))
        .thenReturn(sampleLinks);

    when(eventRepository.findOneByCollectionExerciseAndTag(
            collectionExercise, EventService.Tag.go_live.name()))
        .thenReturn(event);

    // first activate
    sampleSummaryService.activateSamples(collectionExerciseId);
    // then simulate successful enrich
    sampleSummaryService.sampleSummaryValidated(true, collectionExerciseId);
    // then simulate successful distribution
    sampleSummaryService.sampleSummaryDistributed(true, collectionExerciseId);

    verify(collectionExerciseRepository, times(3)).findOneById(collectionExerciseId);
    verify(sampleSummaryActivationPublisher, times(1))
        .sendSampleSummaryActivation(collectionExerciseId, sampleSummaryId, surveyId);
    verify(collectionExerciseService, times(1))
        .transitionCollectionExercise(
            collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.EXECUTE);
    verify(collectionExerciseService, times(1))
        .transitionCollectionExercise(
            collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.VALIDATE);
    verify(collectionExerciseService, times(1))
        .transitionCollectionExercise(
            collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.EXECUTION_COMPLETE);
    verify(collectionExerciseService, times(1))
        .transitionCollectionExercise(
            collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.GO_LIVE);
  }

  @Test
  public void testActivateSamplesFailsEnrichment() throws Exception {
    UUID collectionExerciseId = UUID.randomUUID();
    UUID surveyId = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();

    SampleLink sampleLink = new SampleLink();
    sampleLink.setSampleSummaryId(sampleSummaryId);
    sampleLink.setCollectionExerciseId(collectionExerciseId);

    List<SampleLink> sampleLinks = new ArrayList<>();
    sampleLinks.add(sampleLink);

    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(collectionExerciseId);
    collectionExercise.setSurveyId(surveyId);

    Event event = new Event();
    event.setTimestamp(new Timestamp(System.currentTimeMillis()));

    when(collectionExerciseRepository.findOneById(collectionExerciseId))
        .thenReturn(collectionExercise);
    when(sampleLinkRepository.findByCollectionExerciseId(collectionExerciseId))
        .thenReturn(sampleLinks);

    // first activate
    sampleSummaryService.activateSamples(collectionExerciseId);
    // then simulate failed enrich
    sampleSummaryService.sampleSummaryValidated(false, collectionExerciseId);

    verify(collectionExerciseRepository, times(2)).findOneById(collectionExerciseId);
    verify(sampleSummaryActivationPublisher, times(1))
        .sendSampleSummaryActivation(collectionExerciseId, sampleSummaryId, surveyId);
    verify(collectionExerciseService, times(1))
        .transitionCollectionExercise(
            collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.EXECUTE);
    verify(collectionExerciseService, times(1))
        .transitionCollectionExercise(
            collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.INVALIDATE);
    verify(collectionExerciseService, times(1))
        .transitionCollectionExercise(
            collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.EXECUTION_COMPLETE);
    verify(collectionExerciseService, never())
        .transitionCollectionExercise(
            collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.GO_LIVE);
  }

  @Test
  public void testActivateSamplesFailsDistribution() throws Exception {
    UUID collectionExerciseId = UUID.randomUUID();
    UUID surveyId = UUID.randomUUID();
    UUID sampleSummaryId = UUID.randomUUID();

    SampleLink sampleLink = new SampleLink();
    sampleLink.setSampleSummaryId(sampleSummaryId);
    sampleLink.setCollectionExerciseId(collectionExerciseId);

    List<SampleLink> sampleLinks = new ArrayList<>();
    sampleLinks.add(sampleLink);

    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(collectionExerciseId);
    collectionExercise.setSurveyId(surveyId);

    Event event = new Event();
    event.setTimestamp(new Timestamp(System.currentTimeMillis()));

    when(collectionExerciseRepository.findOneById(collectionExerciseId))
        .thenReturn(collectionExercise);
    when(sampleLinkRepository.findByCollectionExerciseId(collectionExerciseId))
        .thenReturn(sampleLinks);

    // first activate
    sampleSummaryService.activateSamples(collectionExerciseId);
    // then simulate successful enrich
    sampleSummaryService.sampleSummaryValidated(true, collectionExerciseId);
    // then simulate failed distribution
    sampleSummaryService.sampleSummaryDistributed(false, collectionExerciseId);

    verify(collectionExerciseRepository, times(3)).findOneById(collectionExerciseId);
    verify(sampleSummaryActivationPublisher, times(1))
        .sendSampleSummaryActivation(collectionExerciseId, sampleSummaryId, surveyId);
    verify(collectionExerciseService, times(1))
        .transitionCollectionExercise(
            collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.EXECUTE);
    verify(collectionExerciseService, times(1))
        .transitionCollectionExercise(
            collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.VALIDATE);
    verify(collectionExerciseService, times(1))
        .transitionCollectionExercise(
            collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.EXECUTION_COMPLETE);
    verify(collectionExerciseService, never())
        .transitionCollectionExercise(
            collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.GO_LIVE);
  }
}
