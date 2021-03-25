package uk.gov.ons.ctp.response.collection.exercise.service;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent.CI_SAMPLE_DELETED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import uk.gov.ons.ctp.lib.common.FixtureHelper;
import uk.gov.ons.ctp.response.collection.exercise.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SampleSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.state.CollectionExerciseStateTransitionManagerFactory;

/** UnitTests for CollectionExerciseServiceImpl */
@RunWith(MockitoJUnitRunner.class)
public class CollectionExerciseServiceTest {

  @Mock private CollectionExerciseRepository collexRepo;

  @Mock private SampleLinkRepository sampleLinkRepository;

  @Mock private SurveySvcClient surveyService;

  @Mock private CollectionInstrumentSvcClient collectionInstrument;

  @Mock private SampleSvcClient sampleSvcClient;

  @Mock private RabbitTemplate rabbitTemplate;

  @Spy
  private StateTransitionManager<
          CollectionExerciseDTO.CollectionExerciseState,
          CollectionExerciseDTO.CollectionExerciseEvent>
      stateManager =
          (StateTransitionManager<
                  CollectionExerciseDTO.CollectionExerciseState,
                  CollectionExerciseDTO.CollectionExerciseEvent>)
              new CollectionExerciseStateTransitionManagerFactory()
                  .getStateTransitionManager(
                      CollectionExerciseStateTransitionManagerFactory.COLLLECTIONEXERCISE_ENTITY);

  @InjectMocks @Spy private CollectionExerciseService collectionExerciseService;

  /** Tests collection exercise is created with the correct details. */
  @Test
  public void testCreateCollectionExercise() throws Exception {
    // Given
    CollectionExercise collectionExercise =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    when(collexRepo.saveAndFlush(any())).thenReturn(collectionExercise);

    SurveyDTO survey = FixtureHelper.loadClassFixtures(SurveyDTO[].class).get(0);
    CollectionExerciseDTO toCreate =
        FixtureHelper.loadClassFixtures(CollectionExerciseDTO[].class).get(0);
    when(this.surveyService.findSurvey(UUID.fromString(toCreate.getSurveyId()))).thenReturn(survey);

    // When
    this.collectionExerciseService.createCollectionExercise(toCreate, survey);

    // Then
    ArgumentCaptor<CollectionExercise> captor = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(this.collexRepo).saveAndFlush(captor.capture());
    CollectionExercise collex = captor.getValue();
    assertEquals(toCreate.getUserDescription(), collex.getUserDescription());
    assertEquals(toCreate.getExerciseRef(), collex.getExerciseRef());
    assertEquals(toCreate.getSurveyId(), collex.getSurveyId().toString());
    assertNotNull(collex.getCreated());
  }

  @Test
  public void testUpdateCollectionExercise() throws Exception {
    CollectionExerciseDTO toUpdate =
        FixtureHelper.loadClassFixtures(CollectionExerciseDTO[].class).get(0);
    CollectionExercise existing =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    SurveyDTO survey = FixtureHelper.loadClassFixtures(SurveyDTO[].class).get(0);
    UUID surveyId = UUID.fromString(survey.getId());
    existing.setSurveyId(surveyId);
    when(collexRepo.findOneById(existing.getId())).thenReturn(existing);
    when(surveyService.findSurvey(surveyId)).thenReturn(survey);

    this.collectionExerciseService.updateCollectionExercise(existing.getId(), toUpdate);

    ArgumentCaptor<CollectionExercise> captor = ArgumentCaptor.forClass(CollectionExercise.class);

    verify(collexRepo).saveAndFlush(captor.capture());
    CollectionExercise collex = captor.getValue();
    assertEquals(UUID.fromString(toUpdate.getSurveyId()), collex.getSurveyId());
    assertEquals(toUpdate.getExerciseRef(), collex.getExerciseRef());
    assertEquals(toUpdate.getUserDescription(), collex.getUserDescription());
    assertNotNull(collex.getUpdated());
  }

  @Test
  public void testTransitionEventSentWhenTransition() throws Exception {
    // Given
    CollectionExercise existing =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);

    // When
    this.collectionExerciseService.transitionCollectionExercise(
        existing, CollectionExerciseDTO.CollectionExerciseEvent.VALIDATE);

    // Then
    CollectionTransitionEvent collectionTransitionEvent =
        new CollectionTransitionEvent(
            existing.getId(), CollectionExerciseDTO.CollectionExerciseState.VALIDATED);
    verify(rabbitTemplate).convertAndSend(collectionTransitionEvent);
  }

  @Test
  public void testTransitionEventNotSentWhenNoTransition() throws Exception {
    // Given
    CollectionExercise existing =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    existing.setState(CollectionExerciseDTO.CollectionExerciseState.EXECUTION_STARTED);

    // When
    this.collectionExerciseService.transitionCollectionExercise(
        existing, CollectionExerciseDTO.CollectionExerciseEvent.EXECUTE);

    // Then
    verify(rabbitTemplate, never()).convertAndSend(any());
  }

  @Test
  public void testUpdateCollectionExerciseInvalidSurvey() throws Exception {
    CollectionExerciseDTO toUpdate =
        FixtureHelper.loadClassFixtures(CollectionExerciseDTO[].class).get(0);
    CollectionExercise existing =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    existing.setSurveyId(UUID.randomUUID());
    when(collexRepo.findOneById(existing.getId())).thenReturn(existing);

    try {
      this.collectionExerciseService.updateCollectionExercise(existing.getId(), toUpdate);
      fail("Update collection exercise with null survey succeeded");
    } catch (CTPException e) {
      assertEquals(CTPException.Fault.BAD_REQUEST, e.getFault());
    }
  }

  @Test
  public void testUpdateCollectionExerciseNonUnique() throws Exception {
    CollectionExerciseDTO toUpdate =
        FixtureHelper.loadClassFixtures(CollectionExerciseDTO[].class).get(0);
    CollectionExercise existing =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    existing.setSurveyId(UUID.randomUUID());
    // Set up the mock to return the one we are attempting to update
    when(collexRepo.findOneById(existing.getId())).thenReturn(existing);

    UUID uuid = UUID.fromString("0f66744b-bfdb-458a-b495-1eb605462003");
    CollectionExercise otherExisting = new CollectionExercise();
    otherExisting.setId(uuid);
    // Set up the mock to return a different one with the same exercise ref and survey id
    when(collexRepo.findByExerciseRefAndSurveyId(
            toUpdate.getExerciseRef(), UUID.fromString(toUpdate.getSurveyId())))
        .thenReturn(Collections.singletonList(otherExisting));

    try {
      this.collectionExerciseService.updateCollectionExercise(existing.getId(), toUpdate);

      fail("Update to collection exercise breaking uniqueness constraint succeeded");
    } catch (CTPException e) {
      assertEquals(CTPException.Fault.RESOURCE_VERSION_CONFLICT, e.getFault());
    }
  }

  @Test
  public void testUpdateCollectionExerciseDoesNotExist() throws Exception {
    CollectionExerciseDTO toUpdate =
        FixtureHelper.loadClassFixtures(CollectionExerciseDTO[].class).get(0);
    UUID updateUuid = UUID.randomUUID();

    try {
      this.collectionExerciseService.updateCollectionExercise(updateUuid, toUpdate);
      fail("Update of non-existent collection exercise succeeded");
    } catch (CTPException e) {
      assertEquals(CTPException.Fault.RESOURCE_NOT_FOUND, e.getFault());
    }
  }

  @Test
  public void testDeleteCollectionExercise() throws Exception {
    CollectionExercise existing =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    when(collexRepo.findOneById(existing.getId())).thenReturn(existing);

    this.collectionExerciseService.deleteCollectionExercise(existing.getId());

    ArgumentCaptor<CollectionExercise> captor = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(this.collexRepo).saveAndFlush(captor.capture());

    assertEquals(true, captor.getValue().getDeleted());
  }

  @Test
  public void testUndeleteCollectionExercise() throws Exception {
    CollectionExercise existing =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    when(collexRepo.findOneById(existing.getId())).thenReturn(existing);

    this.collectionExerciseService.undeleteCollectionExercise(existing.getId());

    ArgumentCaptor<CollectionExercise> captor = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(this.collexRepo).saveAndFlush(captor.capture());

    assertEquals(false, captor.getValue().getDeleted());
  }

  @Test
  public void testPatchCollectionExerciseNotExists() throws Exception {
    CollectionExerciseDTO toUpdate =
        FixtureHelper.loadClassFixtures(CollectionExerciseDTO[].class).get(0);
    UUID updateUuid = UUID.randomUUID();

    try {
      this.collectionExerciseService.patchCollectionExercise(updateUuid, toUpdate);

      fail("Attempt to patch non-existent collection exercise succeeded");
    } catch (CTPException e) {
      assertEquals(CTPException.Fault.RESOURCE_NOT_FOUND, e.getFault());
    }
  }

  /**
   * Method to setup the member collexRepo with a single collection exercise. This isn't @Before as
   * not all of the tests require a collection exercise to be present (some explicitly do not)
   *
   * @return the collection exercise configured
   * @throws Exception throws if error attempting to load fixtures
   */
  private CollectionExercise setupCollectionExercise() throws Exception {
    CollectionExercise existing =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    existing.setSurveyId(UUID.randomUUID());
    when(collexRepo.findOneById(existing.getId())).thenReturn(existing);

    return existing;
  }

  @Test
  public void testPatchCollectionExerciseExerciseRef() throws Exception {
    CollectionExercise existing = setupCollectionExercise();
    CollectionExerciseDTO collex = new CollectionExerciseDTO();
    SurveyDTO survey = FixtureHelper.loadClassFixtures(SurveyDTO[].class).get(0);
    UUID surveyId = UUID.fromString(survey.getId());
    String exerciseRef = "209966";
    collex.setExerciseRef(exerciseRef);
    collex.setSurveyId(surveyId.toString());
    when(surveyService.findSurvey(surveyId)).thenReturn(survey);
    this.collectionExerciseService.patchCollectionExercise(existing.getId(), collex);

    ArgumentCaptor<CollectionExercise> captor = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(this.collexRepo).saveAndFlush(captor.capture());

    CollectionExercise ce = captor.getValue();
    assertEquals(exerciseRef, ce.getExerciseRef());
    assertNotNull(ce.getUpdated());
  }

  @Test
  public void testPatchCollectionExerciseName() throws Exception {
    CollectionExercise existing = setupCollectionExercise();
    CollectionExerciseDTO collex = new CollectionExerciseDTO();
    String name = "Not BRES";
    SurveyDTO survey = FixtureHelper.loadClassFixtures(SurveyDTO[].class).get(0);
    when(surveyService.findSurvey(any())).thenReturn(survey);
    this.collectionExerciseService.patchCollectionExercise(existing.getId(), collex);

    ArgumentCaptor<CollectionExercise> captor = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(this.collexRepo).saveAndFlush(captor.capture());

    CollectionExercise ce = captor.getValue();
    assertNotNull(ce.getUpdated());
  }

  @Test
  public void testPatchCollectionExerciseUserDescription() throws Exception {
    CollectionExercise existing = setupCollectionExercise();
    CollectionExerciseDTO collex = new CollectionExerciseDTO();
    String userDescription = "Really odd description";
    collex.setUserDescription(userDescription);
    SurveyDTO survey = FixtureHelper.loadClassFixtures(SurveyDTO[].class).get(0);
    when(surveyService.findSurvey(any())).thenReturn(survey);
    this.collectionExerciseService.patchCollectionExercise(existing.getId(), collex);

    ArgumentCaptor<CollectionExercise> captor = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(this.collexRepo).saveAndFlush(captor.capture());

    CollectionExercise ce = captor.getValue();
    assertEquals(userDescription, ce.getUserDescription());
    assertNotNull(ce.getUpdated());
  }

  @Test
  public void testPatchCollectionExerciseNonUnique() throws Exception {
    CollectionExerciseDTO toUpdate =
        FixtureHelper.loadClassFixtures(CollectionExerciseDTO[].class).get(0);
    CollectionExercise existing =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    existing.setSurveyId(UUID.randomUUID());
    // Set up the mock to return the one we are attempting to update
    when(collexRepo.findOneById(existing.getId())).thenReturn(existing);

    UUID uuid = UUID.fromString("0f66744b-bfdb-458a-b495-1eb605462003");
    CollectionExercise otherExisting = new CollectionExercise();
    otherExisting.setId(uuid);
    // Set up the mock to return a different one with the same exercise ref and survey id
    when(collexRepo.findByExerciseRefAndSurveyId(
            toUpdate.getExerciseRef(), UUID.fromString(toUpdate.getSurveyId())))
        .thenReturn(Collections.singletonList(otherExisting));

    try {
      this.collectionExerciseService.patchCollectionExercise(existing.getId(), toUpdate);

      fail("Update to collection exercise breaking uniqueness constraint succeeded");
    } catch (CTPException e) {
      assertEquals(CTPException.Fault.RESOURCE_VERSION_CONFLICT, e.getFault());
    }
  }

  @Test
  public void testTransitionToReadyToReviewWhenScheduledWithCIsAndSample() throws Exception {
    // Given
    CollectionExercise exercise =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    exercise.setState(CollectionExerciseDTO.CollectionExerciseState.SCHEDULED);
    SampleLink testSampleLink = new SampleLink();
    testSampleLink.setSampleSummaryId(UUID.randomUUID());
    given(sampleLinkRepository.findByCollectionExerciseId(exercise.getId()))
        .willReturn(Collections.singletonList(testSampleLink));

    SampleSummaryDTO sampleSummary = new SampleSummaryDTO();
    sampleSummary.setState(SampleSummaryDTO.SampleState.ACTIVE);
    given(sampleSvcClient.getSampleSummary(testSampleLink.getSampleSummaryId()))
        .willReturn(sampleSummary);

    String searchStringJson =
        new JSONObject(Collections.singletonMap("COLLECTION_EXERCISE", exercise.getId().toString()))
            .toString();
    given(collectionInstrument.countCollectionInstruments(searchStringJson)).willReturn(1);

    // When
    collectionExerciseService.transitionScheduleCollectionExerciseToReadyToReview(exercise);

    // Then
    exercise.setState(CollectionExerciseDTO.CollectionExerciseState.READY_FOR_REVIEW);
    verify(collexRepo).saveAndFlush(exercise);
  }

  @Test
  public void testDoNotTransitionToReadyToReviewWhenScheduledWithCIsAndNoSample() throws Exception {
    // Given
    CollectionExercise exercise =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    exercise.setState(CollectionExerciseDTO.CollectionExerciseState.SCHEDULED);
    given(sampleLinkRepository.findByCollectionExerciseId(exercise.getId()))
        .willReturn(Collections.emptyList());
    String searchStringJson =
        new JSONObject(Collections.singletonMap("COLLECTION_EXERCISE", exercise.getId().toString()))
            .toString();
    given(collectionInstrument.countCollectionInstruments(searchStringJson)).willReturn(1);

    // When
    collectionExerciseService.transitionScheduleCollectionExerciseToReadyToReview(exercise);

    // Then
    exercise.setState(CollectionExerciseDTO.CollectionExerciseState.READY_FOR_REVIEW);
    verify(collexRepo, times(0)).saveAndFlush(exercise);
  }

  @Test
  public void testDoNotTransitionToReadyToReviewWhenScheduledWithNoCIsAndSample() throws Exception {
    // Given
    CollectionExercise exercise =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    exercise.setState(CollectionExerciseDTO.CollectionExerciseState.SCHEDULED);
    given(sampleLinkRepository.findByCollectionExerciseId(exercise.getId()))
        .willReturn(Collections.emptyList());
    String searchStringJson =
        new JSONObject(Collections.singletonMap("COLLECTION_EXERCISE", exercise.getId().toString()))
            .toString();
    given(collectionInstrument.countCollectionInstruments(searchStringJson)).willReturn(0);

    // When
    collectionExerciseService.transitionScheduleCollectionExerciseToReadyToReview(exercise);

    // Then
    exercise.setState(CollectionExerciseDTO.CollectionExerciseState.READY_FOR_REVIEW);
    verify(collexRepo, times(0)).saveAndFlush(exercise);
  }

  @Test
  public void testDoNotTransitionToReadyToReviewWhenCIsCountFailsAndReturnsNull() throws Exception {
    // Given
    CollectionExercise exercise =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    exercise.setState(CollectionExerciseDTO.CollectionExerciseState.SCHEDULED);
    given(sampleLinkRepository.findByCollectionExerciseId(exercise.getId()))
        .willReturn(Collections.emptyList());
    String searchStringJson =
        new JSONObject(Collections.singletonMap("COLLECTION_EXERCISE", exercise.getId().toString()))
            .toString();
    given(collectionInstrument.countCollectionInstruments(searchStringJson)).willReturn(null);

    // When
    collectionExerciseService.transitionScheduleCollectionExerciseToReadyToReview(exercise);

    // Then
    exercise.setState(CollectionExerciseDTO.CollectionExerciseState.READY_FOR_REVIEW);
    verify(collexRepo, times(0)).saveAndFlush(exercise);
  }

  @Test
  public void testCreateLink() {
    UUID sampleSummaryUuid = UUID.randomUUID(), collexUuid = UUID.randomUUID();

    when(this.sampleLinkRepository.saveAndFlush(any(SampleLink.class))).then(returnsFirstArg());

    SampleLink sampleLink =
        this.collectionExerciseService.createLink(sampleSummaryUuid, collexUuid);

    assertEquals(sampleSummaryUuid, sampleLink.getSampleSummaryId());
    assertEquals(collexUuid, sampleLink.getCollectionExerciseId());

    verify(sampleLinkRepository, times(1)).saveAndFlush(any());
  }

  @Test
  public void testCreateLinkShouldAttemptToTransitionToReadyToReview() throws CTPException {
    // Given
    UUID sampleSummaryUuid = UUID.randomUUID();
    UUID collexUuid = UUID.randomUUID();
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(collexUuid);
    collectionExercise.setState(CollectionExerciseDTO.CollectionExerciseState.CREATED);
    given(collexRepo.findOneById(collexUuid)).willReturn(collectionExercise);
    given(collectionInstrument.countCollectionInstruments(any())).willReturn(1);
    SampleSummaryDTO sampleSummary = new SampleSummaryDTO();
    SampleLink sampleLink = new SampleLink();
    sampleLink.setSampleSummaryId(sampleSummaryUuid);
    given(sampleLinkRepository.findByCollectionExerciseId(collexUuid))
        .willReturn(Collections.singletonList(sampleLink));
    sampleSummary.setState(SampleSummaryDTO.SampleState.ACTIVE);
    given(sampleSvcClient.getSampleSummary(sampleSummaryUuid)).willReturn(sampleSummary);

    // When
    this.collectionExerciseService.linkSampleSummaryToCollectionExercise(
        collexUuid, Collections.singletonList(sampleSummaryUuid));

    // Then
    verify(stateManager)
        .transition(
            CollectionExerciseDTO.CollectionExerciseState.CREATED,
            CollectionExerciseDTO.CollectionExerciseEvent.CI_SAMPLE_ADDED);
  }

  @Test
  public void testFindCollectionExercisesForSurveys() throws Exception {
    final UUID SURVEY_ID_1 = UUID.fromString("31ec898e-f370-429a-bca4-eab1045aff4e");

    List<UUID> surveys = Arrays.asList(SURVEY_ID_1);

    List<CollectionExercise> existing = FixtureHelper.loadClassFixtures(CollectionExercise[].class);

    given(collexRepo.findBySurveyIdInOrderBySurveyId(surveys)).willReturn(existing);

    HashMap<UUID, List<CollectionExercise>> result =
        this.collectionExerciseService.findCollectionExercisesForSurveys(surveys);

    assertEquals(result.get(SURVEY_ID_1).size(), 2);
  }

  /**
   * Tests that returns collexes in a dictionary (key of survey) when repo returns a list of
   * specific collexes.
   */
  @Test
  public void testFindCollectionExercisesForSurveysByState() throws Exception {
    final UUID SURVEY_ID_1 = UUID.fromString("31ec898e-f370-429a-bca4-eab1045aff4e");

    List<UUID> surveys = Arrays.asList(SURVEY_ID_1);

    List<CollectionExercise> existing = FixtureHelper.loadClassFixtures(CollectionExercise[].class);

    given(
            collexRepo.findBySurveyIdInAndStateOrderBySurveyId(
                surveys, CollectionExerciseDTO.CollectionExerciseState.LIVE))
        .willReturn(existing);

    HashMap<UUID, List<CollectionExercise>> result =
        this.collectionExerciseService.findCollectionExercisesForSurveysByState(
            surveys, CollectionExerciseDTO.CollectionExerciseState.LIVE);

    assertEquals(result.get(SURVEY_ID_1).size(), 2);
  }

  public void testRemoveSampleSummaryLink() throws Exception {
    // Given
    final UUID collectionExerciseId = UUID.fromString("3ec82e0e-18ff-4886-8703-5b83442041ba");
    final UUID sampleSummaryId = UUID.fromString("87043936-4d38-4696-952a-fcd55a51be96");
    final List<SampleLink> emptySampleLinks = new ArrayList<>();

    doNothing()
        .when(collectionExerciseService)
        .transitionCollectionExercise(collectionExerciseId, CI_SAMPLE_DELETED);
    when(sampleLinkRepository.findByCollectionExerciseId(collectionExerciseId))
        .thenReturn(emptySampleLinks);

    // When
    collectionExerciseService.removeSampleSummaryLink(sampleSummaryId, collectionExerciseId);

    // Then
    verify(sampleLinkRepository, times(1))
        .deleteBySampleSummaryIdAndCollectionExerciseId(sampleSummaryId, collectionExerciseId);
    verify(collectionExerciseService, times(1))
        .transitionCollectionExercise(collectionExerciseId, CI_SAMPLE_DELETED);
  }
}
