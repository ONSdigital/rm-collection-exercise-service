package uk.gov.ons.ctp.response.collection.exercise.validation;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.ons.ctp.response.collection.exercise.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.PartySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.lib.collection.instrument.representation.CollectionInstrumentDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.lib.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyClassifierDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyClassifierTypeDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.ExerciseSampleUnitGroupService;

/** Tests for the ValidatesSampleTest */
@RunWith(MockitoJUnitRunner.class)
public class ValidateSampleUnitsTest {

  @Mock private ExerciseSampleUnitGroupService sampleUnitGroupSvc;

  @Mock private SurveySvcClient surveySvcClient;

  @Mock private PartySvcClient partySvcClient;

  @Mock private SampleUnitRepository sampleUnitRepo;

  @Mock private CollectionInstrumentSvcClient collectionInstrumentSvcClient;

  @Mock
  @Qualifier("sampleUnitGroup")
  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupState;

  @Mock private CollectionExerciseService collexService;

  @InjectMocks private ValidateSampleUnits validateSampleUnits;

  private static final String COLLECTION_EXERCISE_ID_1 = "14fb3e68-4dca-46db-bf49-04b84e07e77c";
  private static final String PARTY_ID_1 = "628ed030-19f3-406d-8c1c-b3dc2f4793a0";
  private static final String COLLECTION_INSTRUMENT_ID_1 = "2a6edbe3-0849-48ae-95de-091eb5a08587";
  private static final String COLLECTION_INSTRUMENT_ID_2 = "e9679672-1680-4bd7-bf38-ee9ce44ffdf0";
  private static final String SURVEY_ID = "d943344b-1ef4-4c24-b59b-23e3a0350158";

  @Test
  public void testValidateSampleUnits() throws CTPException {
    // Given
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setSurveyId(UUID.fromString(COLLECTION_EXERCISE_ID_1));
    collectionExercise.setSampleSize(1);
    ExerciseSampleUnitGroup sampleUnitGroup = new ExerciseSampleUnitGroup();
    sampleUnitGroup.setCollectionExercise(collectionExercise);
    ExerciseSampleUnit sampleUnit = new ExerciseSampleUnit();
    sampleUnit.setSampleUnitGroup(sampleUnitGroup);
    sampleUnit.setSampleUnitType(SampleUnitDTO.SampleUnitType.B);
    List<ExerciseSampleUnit> sampleUnits = Collections.singletonList(sampleUnit);
    CollectionInstrumentDTO collectionInstrument = new CollectionInstrumentDTO();
    collectionInstrument.setId(UUID.fromString(COLLECTION_INSTRUMENT_ID_1));
    List<CollectionInstrumentDTO> collectionInstruments =
        Collections.singletonList(collectionInstrument);
    PartyDTO party = new PartyDTO();
    party.setId(PARTY_ID_1);
    SurveyClassifierDTO surveyClassifier = new SurveyClassifierDTO();
    surveyClassifier.setName("COLLECTION_INSTRUMENT");
    surveyClassifier.setId(UUID.randomUUID().toString());
    List<SurveyClassifierDTO> classifierTypeSelectors = Collections.singletonList(surveyClassifier);
    SurveyClassifierTypeDTO classifierTypeSelector = new SurveyClassifierTypeDTO();
    classifierTypeSelector.setClassifierTypes(Collections.emptyList());

    when(sampleUnitRepo.findBySampleUnitGroupCollectionExerciseStateAndSampleUnitGroupStateFK(
            any(), any()))
        .thenReturn(sampleUnits.stream());

    when(surveySvcClient.requestClassifierTypeSelectors(any())).thenReturn(classifierTypeSelectors);

    when(surveySvcClient.requestClassifierTypeSelector(any(), any()))
        .thenReturn(classifierTypeSelector);

    when(collectionInstrumentSvcClient.requestCollectionInstruments(any()))
        .thenReturn(collectionInstruments);

    when(partySvcClient.requestParty(any(), any())).thenReturn(party);

    when(sampleUnitGroupState.transition(any(), any())).thenReturn(SampleUnitGroupState.VALIDATED);

    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupState.INIT), any(CollectionExercise.class)))
        .thenReturn(0L);

    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupState.VALIDATED), any(CollectionExercise.class)))
        .thenReturn(1L);

    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupState.FAILEDVALIDATION), any(CollectionExercise.class)))
        .thenReturn(0L);

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    ArgumentCaptor<ExerciseSampleUnit> exerciseSampleUnitArgCapt =
        ArgumentCaptor.forClass(ExerciseSampleUnit.class);
    verify(sampleUnitRepo).save(exerciseSampleUnitArgCapt.capture());
    assertEquals(
        SampleUnitGroupState.VALIDATED,
        exerciseSampleUnitArgCapt.getValue().getSampleUnitGroup().getStateFK());
    assertEquals(PARTY_ID_1, exerciseSampleUnitArgCapt.getValue().getPartyId().toString());
    assertEquals(
        COLLECTION_INSTRUMENT_ID_1,
        exerciseSampleUnitArgCapt.getValue().getCollectionInstrumentId().toString());
    verify(collexService)
        .transitionCollectionExercise(collectionExercise, CollectionExerciseEvent.VALIDATE);
  }

  /*
   * This test covers the scenario where a collection exercise has multiple collection instruments associated
   * with it. Usually (virtually 100% of the time) a single sample will be uploaded and processed in isolation
   * (i.e. it would be very rare to have 2 or more samples validating at the same time) and we are mainly interested
   * in the collection instruments which relate to the form types contained in the sample which was uploaded.
   * An MBS sample, for example, may contain as many as 20 different collection instruments.
   */
  @Test
  public void testValidateSampleUnitsMultipleCollectionInstruments() throws CTPException {
    // Given
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setSurveyId(UUID.fromString(SURVEY_ID));
    collectionExercise.setSampleSize(2);

    List<ExerciseSampleUnit> sampleUnits = new LinkedList<>();

    ExerciseSampleUnitGroup sampleUnitGroup = new ExerciseSampleUnitGroup();
    sampleUnitGroup.setCollectionExercise(collectionExercise);
    sampleUnitGroup.setFormType("0101");
    ExerciseSampleUnit sampleUnit = new ExerciseSampleUnit();
    sampleUnit.setSampleUnitGroup(sampleUnitGroup);
    sampleUnit.setSampleUnitType(SampleUnitDTO.SampleUnitType.B);
    sampleUnits.add(sampleUnit); // Add the first sample

    sampleUnitGroup = new ExerciseSampleUnitGroup();
    sampleUnitGroup.setCollectionExercise(collectionExercise);
    sampleUnitGroup.setFormType("0666");
    sampleUnit = new ExerciseSampleUnit();
    sampleUnit.setSampleUnitGroup(sampleUnitGroup);
    sampleUnit.setSampleUnitType(SampleUnitDTO.SampleUnitType.B);
    sampleUnits.add(sampleUnit); // Add the second sample

    CollectionInstrumentDTO collectionInstrument = new CollectionInstrumentDTO();
    collectionInstrument.setId(UUID.fromString(COLLECTION_INSTRUMENT_ID_1));
    List<CollectionInstrumentDTO> collectionInstrumentsOne =
        Collections.singletonList(collectionInstrument);

    collectionInstrument = new CollectionInstrumentDTO();
    collectionInstrument.setId(UUID.fromString(COLLECTION_INSTRUMENT_ID_2));
    List<CollectionInstrumentDTO> collectionInstrumentsTwo =
        Collections.singletonList(collectionInstrument);

    PartyDTO party = new PartyDTO();
    party.setId(PARTY_ID_1);
    SurveyClassifierDTO surveyClassifier = new SurveyClassifierDTO();
    surveyClassifier.setName("COLLECTION_INSTRUMENT");
    surveyClassifier.setId(UUID.randomUUID().toString());
    List<SurveyClassifierDTO> classifierTypeSelectors = Collections.singletonList(surveyClassifier);
    SurveyClassifierTypeDTO classifierTypeSelector = new SurveyClassifierTypeDTO();
    classifierTypeSelector.setClassifierTypes(Collections.singletonList("FORM_TYPE"));

    when(sampleUnitRepo.findBySampleUnitGroupCollectionExerciseStateAndSampleUnitGroupStateFK(
            any(), any()))
        .thenReturn(sampleUnits.stream());

    when(surveySvcClient.requestClassifierTypeSelectors(any())).thenReturn(classifierTypeSelectors);

    when(surveySvcClient.requestClassifierTypeSelector(any(), any()))
        .thenReturn(classifierTypeSelector);

    when(collectionInstrumentSvcClient.requestCollectionInstruments(
            eq("{\"SURVEY_ID\":\"d943344b-1ef4-4c24-b59b-23e3a0350158\",\"FORM_TYPE\":\"0101\"}")))
        .thenReturn(collectionInstrumentsOne);

    when(collectionInstrumentSvcClient.requestCollectionInstruments(
            eq("{\"SURVEY_ID\":\"d943344b-1ef4-4c24-b59b-23e3a0350158\",\"FORM_TYPE\":\"0666\"}")))
        .thenReturn(collectionInstrumentsTwo);

    when(partySvcClient.requestParty(any(), any())).thenReturn(party);

    when(sampleUnitGroupState.transition(any(), any())).thenReturn(SampleUnitGroupState.VALIDATED);

    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupState.INIT), any(CollectionExercise.class)))
        .thenReturn(0L);

    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupState.VALIDATED), any(CollectionExercise.class)))
        .thenReturn(1L);

    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupState.FAILEDVALIDATION), any(CollectionExercise.class)))
        .thenReturn(0L);

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    ArgumentCaptor<ExerciseSampleUnit> exerciseSampleUnitArgCapt =
        ArgumentCaptor.forClass(ExerciseSampleUnit.class);
    verify(sampleUnitRepo, times(2)).save(exerciseSampleUnitArgCapt.capture());
    List<ExerciseSampleUnit> exerciseSampleUnits = exerciseSampleUnitArgCapt.getAllValues();

    assertThat(
        exerciseSampleUnits,
        containsInAnyOrder(
            hasProperty("collectionInstrumentId", is(UUID.fromString(COLLECTION_INSTRUMENT_ID_1))),
            hasProperty(
                "collectionInstrumentId", is(UUID.fromString(COLLECTION_INSTRUMENT_ID_2)))));
  }

  @Test
  public void testValidateNoSampleUnits() throws CTPException {
    // Given
    List<ExerciseSampleUnit> sampleUnits = Collections.emptyList();

    when(sampleUnitRepo.findBySampleUnitGroupCollectionExerciseStateAndSampleUnitGroupStateFK(
            any(), any()))
        .thenReturn(sampleUnits.stream());

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(sampleUnitRepo, never()).save(any(ExerciseSampleUnit.class));
    verify(collexService, never())
        .transitionCollectionExercise(any(CollectionExercise.class), any());
  }

  @Test(expected = HttpClientErrorException.class)
  public void testValidateSampleUnitsRequestPartyFail() {
    // Given
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setSurveyId(UUID.fromString(COLLECTION_EXERCISE_ID_1));
    collectionExercise.setSampleSize(1);
    ExerciseSampleUnitGroup sampleUnitGroup = new ExerciseSampleUnitGroup();
    sampleUnitGroup.setCollectionExercise(collectionExercise);
    ExerciseSampleUnit sampleUnit = new ExerciseSampleUnit();
    sampleUnit.setSampleUnitGroup(sampleUnitGroup);
    sampleUnit.setSampleUnitType(SampleUnitDTO.SampleUnitType.B);
    List<ExerciseSampleUnit> sampleUnits = Collections.singletonList(sampleUnit);
    CollectionInstrumentDTO collectionInstrument = new CollectionInstrumentDTO();
    List<CollectionInstrumentDTO> collectionInstruments =
        Collections.singletonList(collectionInstrument);
    SurveyClassifierDTO surveyClassifier = new SurveyClassifierDTO();
    surveyClassifier.setName("COLLECTION_INSTRUMENT");
    surveyClassifier.setId(UUID.randomUUID().toString());
    List<SurveyClassifierDTO> classifierTypeSelectors = Collections.singletonList(surveyClassifier);
    SurveyClassifierTypeDTO classifierTypeSelector = new SurveyClassifierTypeDTO();
    classifierTypeSelector.setClassifierTypes(Collections.emptyList());

    when(sampleUnitRepo.findBySampleUnitGroupCollectionExerciseStateAndSampleUnitGroupStateFK(
            any(), any()))
        .thenReturn(sampleUnits.stream());

    when(surveySvcClient.requestClassifierTypeSelectors(any())).thenReturn(classifierTypeSelectors);

    when(surveySvcClient.requestClassifierTypeSelector(any(), any()))
        .thenReturn(classifierTypeSelector);

    when(collectionInstrumentSvcClient.requestCollectionInstruments(any()))
        .thenReturn(collectionInstruments);

    when(partySvcClient.requestParty(any(), any()))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error"));

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    // ... exception happens, transaction rolled back
  }

  @Test
  public void testValidateSampleUnitsCollectionInstrumentNotFound() throws CTPException {
    // Given
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setSurveyId(UUID.fromString(COLLECTION_EXERCISE_ID_1));
    collectionExercise.setSampleSize(1);
    ExerciseSampleUnitGroup sampleUnitGroup = new ExerciseSampleUnitGroup();
    sampleUnitGroup.setCollectionExercise(collectionExercise);
    ExerciseSampleUnit sampleUnit = new ExerciseSampleUnit();
    sampleUnit.setSampleUnitGroup(sampleUnitGroup);
    sampleUnit.setSampleUnitType(SampleUnitDTO.SampleUnitType.B);
    List<ExerciseSampleUnit> sampleUnits = Collections.singletonList(sampleUnit);
    PartyDTO party = new PartyDTO();
    party.setId(PARTY_ID_1);
    SurveyClassifierDTO surveyClassifier = new SurveyClassifierDTO();
    surveyClassifier.setName("COLLECTION_INSTRUMENT");
    surveyClassifier.setId(UUID.randomUUID().toString());
    List<SurveyClassifierDTO> classifierTypeSelectors = Collections.singletonList(surveyClassifier);
    SurveyClassifierTypeDTO classifierTypeSelector = new SurveyClassifierTypeDTO();
    classifierTypeSelector.setClassifierTypes(Collections.emptyList());

    when(sampleUnitRepo.findBySampleUnitGroupCollectionExerciseStateAndSampleUnitGroupStateFK(
            any(), any()))
        .thenReturn(sampleUnits.stream());

    when(surveySvcClient.requestClassifierTypeSelectors(any())).thenReturn(classifierTypeSelectors);

    when(surveySvcClient.requestClassifierTypeSelector(any(), any()))
        .thenReturn(classifierTypeSelector);

    when(collectionInstrumentSvcClient.requestCollectionInstruments(any()))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Bad stuff happened"));

    when(partySvcClient.requestParty(any(), any())).thenReturn(party);

    when(sampleUnitGroupState.transition(any(), any()))
        .thenReturn(SampleUnitGroupState.FAILEDVALIDATION);

    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupState.INIT), any(CollectionExercise.class)))
        .thenReturn(0L);

    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupState.VALIDATED), any(CollectionExercise.class)))
        .thenReturn(0L);

    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupState.FAILEDVALIDATION), any(CollectionExercise.class)))
        .thenReturn(1L);

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    ArgumentCaptor<ExerciseSampleUnit> exerciseSampleUnitArgCapt =
        ArgumentCaptor.forClass(ExerciseSampleUnit.class);
    verify(sampleUnitRepo).save(exerciseSampleUnitArgCapt.capture());
    assertEquals(
        SampleUnitGroupState.FAILEDVALIDATION,
        exerciseSampleUnitArgCapt.getValue().getSampleUnitGroup().getStateFK());
    assertEquals(PARTY_ID_1, exerciseSampleUnitArgCapt.getValue().getPartyId().toString());
    assertNull(exerciseSampleUnitArgCapt.getValue().getCollectionInstrumentId());
    verify(collexService)
        .transitionCollectionExercise(collectionExercise, CollectionExerciseEvent.INVALIDATE);
  }

  @Test(expected = HttpClientErrorException.class)
  public void testValidateSampleUnitsCollectionInstrumentFail() {
    // Given
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setSurveyId(UUID.fromString(COLLECTION_EXERCISE_ID_1));
    collectionExercise.setSampleSize(1);
    ExerciseSampleUnitGroup sampleUnitGroup = new ExerciseSampleUnitGroup();
    sampleUnitGroup.setCollectionExercise(collectionExercise);
    ExerciseSampleUnit sampleUnit = new ExerciseSampleUnit();
    sampleUnit.setSampleUnitGroup(sampleUnitGroup);
    sampleUnit.setSampleUnitType(SampleUnitDTO.SampleUnitType.B);
    List<ExerciseSampleUnit> sampleUnits = Collections.singletonList(sampleUnit);
    PartyDTO party = new PartyDTO();
    party.setId(PARTY_ID_1);
    SurveyClassifierDTO surveyClassifier = new SurveyClassifierDTO();
    surveyClassifier.setName("COLLECTION_INSTRUMENT");
    surveyClassifier.setId(UUID.randomUUID().toString());
    List<SurveyClassifierDTO> classifierTypeSelectors = Collections.singletonList(surveyClassifier);
    SurveyClassifierTypeDTO classifierTypeSelector = new SurveyClassifierTypeDTO();
    classifierTypeSelector.setClassifierTypes(Collections.emptyList());

    when(sampleUnitRepo.findBySampleUnitGroupCollectionExerciseStateAndSampleUnitGroupStateFK(
            any(), any()))
        .thenReturn(sampleUnits.stream());

    when(surveySvcClient.requestClassifierTypeSelectors(any())).thenReturn(classifierTypeSelectors);

    when(surveySvcClient.requestClassifierTypeSelector(any(), any()))
        .thenReturn(classifierTypeSelector);

    when(collectionInstrumentSvcClient.requestCollectionInstruments(any()))
        .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error"));

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    // ... exception happens, transaction rolled back
  }

  @Test
  public void testValidateSampleUnitsPartyNotFound() throws CTPException {
    // Given
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setSurveyId(UUID.fromString(COLLECTION_EXERCISE_ID_1));
    collectionExercise.setSampleSize(1);
    ExerciseSampleUnitGroup sampleUnitGroup = new ExerciseSampleUnitGroup();
    sampleUnitGroup.setCollectionExercise(collectionExercise);
    ExerciseSampleUnit sampleUnit = new ExerciseSampleUnit();
    sampleUnit.setSampleUnitGroup(sampleUnitGroup);
    sampleUnit.setSampleUnitType(SampleUnitDTO.SampleUnitType.B);
    List<ExerciseSampleUnit> sampleUnits = Collections.singletonList(sampleUnit);
    CollectionInstrumentDTO collectionInstrument = new CollectionInstrumentDTO();
    collectionInstrument.setId(UUID.fromString(COLLECTION_INSTRUMENT_ID_1));
    List<CollectionInstrumentDTO> collectionInstruments =
        Collections.singletonList(collectionInstrument);
    PartyDTO party = new PartyDTO();
    party.setId(PARTY_ID_1);
    SurveyClassifierDTO surveyClassifier = new SurveyClassifierDTO();
    surveyClassifier.setName("COLLECTION_INSTRUMENT");
    surveyClassifier.setId(UUID.randomUUID().toString());
    List<SurveyClassifierDTO> classifierTypeSelectors = Collections.singletonList(surveyClassifier);
    SurveyClassifierTypeDTO classifierTypeSelector = new SurveyClassifierTypeDTO();
    classifierTypeSelector.setClassifierTypes(Collections.emptyList());

    when(sampleUnitRepo.findBySampleUnitGroupCollectionExerciseStateAndSampleUnitGroupStateFK(
            any(), any()))
        .thenReturn(sampleUnits.stream());

    when(surveySvcClient.requestClassifierTypeSelectors(any())).thenReturn(classifierTypeSelectors);

    when(surveySvcClient.requestClassifierTypeSelector(any(), any()))
        .thenReturn(classifierTypeSelector);

    when(collectionInstrumentSvcClient.requestCollectionInstruments(any()))
        .thenReturn(collectionInstruments);

    when(partySvcClient.requestParty(any(), any()))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Bad stuff happened"));

    when(sampleUnitGroupState.transition(any(), any()))
        .thenReturn(SampleUnitGroupState.FAILEDVALIDATION);

    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupState.INIT), any(CollectionExercise.class)))
        .thenReturn(0L);

    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupState.VALIDATED), any(CollectionExercise.class)))
        .thenReturn(0L);

    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupState.FAILEDVALIDATION), any(CollectionExercise.class)))
        .thenReturn(1L);

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    ArgumentCaptor<ExerciseSampleUnit> exerciseSampleUnitArgCapt =
        ArgumentCaptor.forClass(ExerciseSampleUnit.class);
    verify(sampleUnitRepo).save(exerciseSampleUnitArgCapt.capture());
    assertEquals(
        SampleUnitGroupState.FAILEDVALIDATION,
        exerciseSampleUnitArgCapt.getValue().getSampleUnitGroup().getStateFK());
    assertNull(exerciseSampleUnitArgCapt.getValue().getPartyId());
    assertEquals(
        COLLECTION_INSTRUMENT_ID_1,
        exerciseSampleUnitArgCapt.getValue().getCollectionInstrumentId().toString());
    verify(collexService)
        .transitionCollectionExercise(collectionExercise, CollectionExerciseEvent.INVALIDATE);
  }

  @Test(expected = RuntimeException.class)
  public void testValidateSampleUnitsSurveyTypeSelectorsBlowsUp() {
    // Given
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setSurveyId(UUID.fromString(COLLECTION_EXERCISE_ID_1));
    collectionExercise.setSampleSize(1);
    ExerciseSampleUnitGroup sampleUnitGroup = new ExerciseSampleUnitGroup();
    sampleUnitGroup.setCollectionExercise(collectionExercise);
    ExerciseSampleUnit sampleUnit = new ExerciseSampleUnit();
    sampleUnit.setSampleUnitGroup(sampleUnitGroup);
    sampleUnit.setSampleUnitType(SampleUnitDTO.SampleUnitType.B);
    List<ExerciseSampleUnit> sampleUnits = Collections.singletonList(sampleUnit);

    when(sampleUnitRepo.findBySampleUnitGroupCollectionExerciseStateAndSampleUnitGroupStateFK(
            any(), any()))
        .thenReturn(sampleUnits.stream());

    when(surveySvcClient.requestClassifierTypeSelectors(any()))
        .thenThrow(new RuntimeException("Kaboom"));

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    // ... exception happens, transaction rolled back
  }

  @Test(expected = RuntimeException.class)
  public void testValidateSampleUnitsSurveyTypeSelectorBlowsUp() {
    // Given
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setSurveyId(UUID.fromString(COLLECTION_EXERCISE_ID_1));
    collectionExercise.setSampleSize(1);
    ExerciseSampleUnitGroup sampleUnitGroup = new ExerciseSampleUnitGroup();
    sampleUnitGroup.setCollectionExercise(collectionExercise);
    ExerciseSampleUnit sampleUnit = new ExerciseSampleUnit();
    sampleUnit.setSampleUnitGroup(sampleUnitGroup);
    sampleUnit.setSampleUnitType(SampleUnitDTO.SampleUnitType.B);
    List<ExerciseSampleUnit> sampleUnits = Collections.singletonList(sampleUnit);
    SurveyClassifierDTO surveyClassifier = new SurveyClassifierDTO();
    surveyClassifier.setName("COLLECTION_INSTRUMENT");
    surveyClassifier.setId(UUID.randomUUID().toString());
    List<SurveyClassifierDTO> classifierTypeSelectors = Collections.singletonList(surveyClassifier);

    when(sampleUnitRepo.findBySampleUnitGroupCollectionExerciseStateAndSampleUnitGroupStateFK(
            any(), any()))
        .thenReturn(sampleUnits.stream());

    when(surveySvcClient.requestClassifierTypeSelectors(any())).thenReturn(classifierTypeSelectors);

    when(surveySvcClient.requestClassifierTypeSelector(any(), any()))
        .thenThrow(new RuntimeException("Kablammo"));

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    // ... exception happens, transaction rolled back
  }

  @Test
  public void testTransitionExecutionStartedCollex() throws CTPException {
    CollectionExercise collex = new CollectionExercise();
    collex.setSampleSize(99);
    List<ExerciseSampleUnit> sampleUnits = Collections.emptyList();

    // Given
    when(collexService.findByState(CollectionExerciseState.EXECUTION_STARTED))
        .thenReturn(Collections.singletonList(collex));
    when(sampleUnitRepo.countBySampleUnitGroupCollectionExercise(collex)).thenReturn(99);
    when(sampleUnitRepo.findBySampleUnitGroupCollectionExerciseStateAndSampleUnitGroupStateFK(
            any(), any()))
        .thenReturn(sampleUnits.stream());

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(collexService)
        .transitionCollectionExercise(eq(collex), eq(CollectionExerciseEvent.EXECUTION_COMPLETE));
  }

  @Test
  public void testShouldNotTransitionExecutionStartedUnfinishedCollex() throws CTPException {
    CollectionExercise collex = new CollectionExercise();
    collex.setSampleSize(66);
    List<ExerciseSampleUnit> sampleUnits = Collections.emptyList();

    // Given
    when(collexService.findByState(CollectionExerciseState.EXECUTION_STARTED))
        .thenReturn(Collections.singletonList(collex));
    when(sampleUnitRepo.countBySampleUnitGroupCollectionExercise(collex)).thenReturn(33);
    when(sampleUnitRepo.findBySampleUnitGroupCollectionExerciseStateAndSampleUnitGroupStateFK(
            any(), any()))
        .thenReturn(sampleUnits.stream());

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(collexService, never())
        .transitionCollectionExercise(
            any(CollectionExercise.class), eq(CollectionExerciseEvent.EXECUTION_COMPLETE));
  }
}
