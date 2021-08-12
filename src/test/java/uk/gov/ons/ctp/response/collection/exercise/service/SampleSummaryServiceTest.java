package uk.gov.ons.ctp.response.collection.exercise.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.client.SampleSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class SampleSummaryServiceTest {

  @Mock
  private SampleLinkRepository sampleLinkRepository;

  @Mock
  private CollectionExerciseRepository collectionExerciseRepository;

  @Mock
  private SampleSvcClient sampleSvcClient;

  @Mock
  private CollectionExerciseService collectionExerciseService;

  @Mock
  private EventRepository eventRepository;

  @InjectMocks
  private SampleSummaryService sampleSummaryService;

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

    when(collectionExerciseRepository.findOneById(collectionExerciseId)).thenReturn(collectionExercise);
    when(sampleLinkRepository.findByCollectionExerciseId(collectionExerciseId)).thenReturn(sampleLinks);
    when(sampleSvcClient.enrichSampleSummary(surveyId, collectionExerciseId, sampleSummaryId)).thenReturn(true);
    when(sampleSvcClient.distributeSampleSummary(sampleSummaryId)).thenReturn(true);
    when(eventRepository.findOneByCollectionExerciseAndTag(collectionExercise, EventService.Tag.go_live.name())).thenReturn(event);

    assertTrue("samples processed successfully", sampleSummaryService.activateSamples(collectionExerciseId));

    verify(collectionExerciseRepository, times(3)).findOneById(collectionExerciseId);
    verify( collectionExerciseService, times(1)).transitionCollectionExercise(collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.EXECUTE);
    verify( collectionExerciseService, times(1)).transitionCollectionExercise(collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.VALIDATE);
    verify( collectionExerciseService, times(1)).transitionCollectionExercise(collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.EXECUTION_COMPLETE);
    verify( collectionExerciseService, times(1)).transitionCollectionExercise(collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.GO_LIVE);
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

    when(collectionExerciseRepository.findOneById(collectionExerciseId)).thenReturn(collectionExercise);
    when(sampleLinkRepository.findByCollectionExerciseId(collectionExerciseId)).thenReturn(sampleLinks);
    when(sampleSvcClient.enrichSampleSummary(surveyId, collectionExerciseId, sampleSummaryId)).thenReturn(false);

    assertFalse(sampleSummaryService.activateSamples(collectionExerciseId));

    verify(collectionExerciseRepository, times(2)).findOneById(collectionExerciseId);
    verify( collectionExerciseService, times(1)).transitionCollectionExercise(collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.EXECUTE);
    verify( collectionExerciseService, times(1)).transitionCollectionExercise(collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.INVALIDATE);
    verify( collectionExerciseService, times(1)).transitionCollectionExercise(collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.EXECUTION_COMPLETE);
    verify( collectionExerciseService, never()).transitionCollectionExercise(collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.GO_LIVE);
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

    when(collectionExerciseRepository.findOneById(collectionExerciseId)).thenReturn(collectionExercise);
    when(sampleLinkRepository.findByCollectionExerciseId(collectionExerciseId)).thenReturn(sampleLinks);
    when(sampleSvcClient.enrichSampleSummary(surveyId, collectionExerciseId, sampleSummaryId)).thenReturn(true);
    when(sampleSvcClient.distributeSampleSummary(sampleSummaryId)).thenReturn(false);

    assertFalse(sampleSummaryService.activateSamples(collectionExerciseId));

    verify(collectionExerciseRepository, times(3)).findOneById(collectionExerciseId);
    verify( collectionExerciseService, times(1)).transitionCollectionExercise(collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.EXECUTE);
    verify( collectionExerciseService, times(1)).transitionCollectionExercise(collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.VALIDATE);
    verify( collectionExerciseService, times(1)).transitionCollectionExercise(collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.EXECUTION_COMPLETE);
    verify( collectionExerciseService, never()).transitionCollectionExercise(collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.GO_LIVE);
  }
}
