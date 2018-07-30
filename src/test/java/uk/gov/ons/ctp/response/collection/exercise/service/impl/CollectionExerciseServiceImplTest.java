package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent.CI_SAMPLE_DELETED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SampleSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeDefault;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeDefaultRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeOverrideRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionTransitionEvent;
import uk.gov.ons.ctp.response.collection.exercise.service.SurveyService;
import uk.gov.ons.ctp.response.collection.exercise.state.CollectionExerciseStateTransitionManagerFactory;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO;

/** UnitTests for CollectionExerciseServiceImpl */
@RunWith(MockitoJUnitRunner.class)
public class CollectionExerciseServiceImplTest {

  private static final UUID ACTIONPLANID1 = UUID.fromString("60df56d9-f491-4ac8-b256-a10154290a8b");
  private static final UUID ACTIONPLANID2 = UUID.fromString("60df56d9-f491-4ac8-b256-a10154290a8c");
  private static final UUID ACTIONPLANID3 = UUID.fromString("70df56d9-f491-4ac8-b256-a10154290a8b");
  private static final UUID ACTIONPLANID4 = UUID.fromString("80df56d9-f491-4ac8-b256-a10154290a8b");

  @Mock private CaseTypeDefaultRepository caseTypeDefaultRepo;

  @Mock private CaseTypeOverrideRepository caseTypeOverrideRepo;

  @Mock private CollectionExerciseRepository collexRepo;

  @Mock private SampleLinkRepository sampleLinkRepository;

  @Mock private SurveyService surveyService;

  @Mock private ActionSvcClient actionService;

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

  @InjectMocks @Spy private CollectionExerciseServiceImpl collectionExerciseServiceImpl;

  /** Tests that default and override are empty */
  @Test
  public void givenThatDefaultAndOverrideAreEmptyExpectEmpty() {
    List<CaseTypeDefault> caseTypeDefaultList = new ArrayList<>();
    List<CaseTypeOverride> caseTypeOverrideList = new ArrayList<>();

    Collection<CaseType> caseTypeList =
        this.collectionExerciseServiceImpl.createCaseTypeList(
            caseTypeDefaultList, caseTypeOverrideList);

    Assert.assertTrue(caseTypeList.isEmpty());
  }

  /** Check that only default are present is Override is empty */
  @Test
  public void givenThatDefaultIsPopulatedAndOverrideIsEmptyExpectDefaultOnly() {
    List<CaseTypeDefault> caseTypeDefaultList = new ArrayList<>();
    List<CaseTypeOverride> caseTypeOverrideList = new ArrayList<>();

    caseTypeDefaultList.add(createCaseTypeDefault(ACTIONPLANID1, "B"));

    Collection<CaseType> caseTypeList =
        this.collectionExerciseServiceImpl.createCaseTypeList(
            caseTypeDefaultList, caseTypeOverrideList);

    assertEquals(caseTypeList.iterator().next().getActionPlanId(), ACTIONPLANID1);
  }

  @Test
  public void givenThatDefaultIsEmptyAndOverrideIsPopulatedExpectEmpty() {
    List<CaseTypeDefault> caseTypeDefaultList = new ArrayList<>();
    List<CaseTypeOverride> caseTypeOverrideList = new ArrayList<>();

    caseTypeOverrideList.add(createCaseTypeOverride(ACTIONPLANID3, "B"));

    Collection<CaseType> caseTypeList =
        this.collectionExerciseServiceImpl.createCaseTypeList(
            caseTypeDefaultList, caseTypeOverrideList);

    assertEquals(caseTypeList.iterator().next().getActionPlanId(), ACTIONPLANID3);
  }

  @Test
  public void givenThatDefaultIsPopulatedAndOverrideIsPopulatedExpectOnlyOverride() {
    List<CaseTypeDefault> caseTypeDefaultList = new ArrayList<>();
    List<CaseTypeOverride> caseTypeOverrideList = new ArrayList<>();

    caseTypeDefaultList.add(createCaseTypeDefault(ACTIONPLANID1, "B"));

    caseTypeOverrideList.add(createCaseTypeOverride(ACTIONPLANID3, "B"));

    Collection<CaseType> caseTypeList =
        this.collectionExerciseServiceImpl.createCaseTypeList(
            caseTypeDefaultList, caseTypeOverrideList);

    assertEquals(caseTypeList.iterator().next().getActionPlanId(), ACTIONPLANID3);
  }

  @Test
  public void
      givenThatDefaultIsPopulatedWithMultipleAndOverrideIsPopulatedWithOneExpectOneDefaultAndOneOverride() {

    List<CaseTypeDefault> caseTypeDefaultList = new ArrayList<>();
    List<CaseTypeOverride> caseTypeOverrideList = new ArrayList<>();

    caseTypeDefaultList.add(createCaseTypeDefault(ACTIONPLANID1, "B"));
    caseTypeDefaultList.add(createCaseTypeDefault(ACTIONPLANID2, "BI"));

    caseTypeOverrideList.add(createCaseTypeOverride(ACTIONPLANID3, "BI"));

    Collection<CaseType> caseTypeList =
        this.collectionExerciseServiceImpl.createCaseTypeList(
            caseTypeDefaultList, caseTypeOverrideList);
    Iterator<CaseType> i = caseTypeList.iterator();
    assertEquals(i.next().getActionPlanId(), ACTIONPLANID1);
    assertEquals(i.next().getActionPlanId(), ACTIONPLANID3);
  }

  @Test
  public void
      givenThatDefaultIsPopulatedWithMultipleAndOverrideIsPopulatedWithMultipleExpectMultipleOverride() {

    List<CaseTypeDefault> caseTypeDefaultList = new ArrayList<>();
    List<CaseTypeOverride> caseTypeOverrideList = new ArrayList<>();

    caseTypeDefaultList.add(createCaseTypeDefault(ACTIONPLANID1, "B"));
    caseTypeDefaultList.add(createCaseTypeDefault(ACTIONPLANID2, "BI"));

    caseTypeOverrideList.add(createCaseTypeOverride(ACTIONPLANID3, "B"));
    caseTypeOverrideList.add(createCaseTypeOverride(ACTIONPLANID4, "BI"));

    Collection<CaseType> caseTypeList =
        this.collectionExerciseServiceImpl.createCaseTypeList(
            caseTypeDefaultList, caseTypeOverrideList);

    Iterator<CaseType> i = caseTypeList.iterator();
    assertEquals(i.next().getActionPlanId(), ACTIONPLANID3);
    assertEquals(i.next().getActionPlanId(), ACTIONPLANID4);
  }

  /**
   * Creates a Defualt Case Type
   *
   * @param actionPlanId actionPlanId to be used
   * @param sampleUnitTypeFK sampleUnitTypeFK as string
   * @return CaseTypeDefault
   */
  private CaseTypeDefault createCaseTypeDefault(UUID actionPlanId, String sampleUnitTypeFK) {
    CaseTypeDefault.builder().caseTypeDefaultPK(1).surveyId(UUID.randomUUID());
    return CaseTypeDefault.builder()
        .actionPlanId(actionPlanId)
        .sampleUnitTypeFK(sampleUnitTypeFK)
        .build();
  }

  /**
   * Creates a Default Case Type
   *
   * @param actionPlanId actionPlanId to be used
   * @param sampleUnitTypeFK sampleUnitTypeFK as string
   * @return CaseTypeOverride
   */
  private CaseTypeOverride createCaseTypeOverride(UUID actionPlanId, String sampleUnitTypeFK) {
    CaseTypeOverride.builder().caseTypeOverridePK(1).exerciseFK(1);
    return CaseTypeOverride.builder()
        .actionPlanId(actionPlanId)
        .sampleUnitTypeFK(sampleUnitTypeFK)
        .build();
  }

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

    ActionPlanDTO actionPlanDTO = new ActionPlanDTO();
    actionPlanDTO.setId(UUID.randomUUID());
    when(actionService.createActionPlan(any(), any(), any())).thenReturn(actionPlanDTO);
    when(caseTypeDefaultRepo.findTopBySurveyIdAndSampleUnitTypeFK(any(), any())).thenReturn(null);

    // When
    this.collectionExerciseServiceImpl.createCollectionExercise(toCreate, survey);

    // Then
    ArgumentCaptor<CollectionExercise> captor = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(this.collexRepo).saveAndFlush(captor.capture());
    CollectionExercise collex = captor.getValue();
    assertEquals(toCreate.getName(), collex.getName());
    assertEquals(toCreate.getUserDescription(), collex.getUserDescription());
    assertEquals(toCreate.getExerciseRef(), collex.getExerciseRef());
    assertEquals(toCreate.getSurveyId(), collex.getSurveyId().toString());
    assertNotNull(collex.getCreated());
    verify(this.caseTypeOverrideRepo, times(2)).saveAndFlush(any());
  }

  /** Tests that create collection exercise endpoint creates the action plans. */
  @Test
  public void testCreateCollectionExerciseCreatesTheActionPlans() throws Exception {
    // Given
    CollectionExerciseDTO toCreate =
        FixtureHelper.loadClassFixtures(CollectionExerciseDTO[].class).get(0);
    CollectionExercise collectionExercise =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    collectionExercise.setExerciseRef(toCreate.getExerciseRef());
    when(collexRepo.saveAndFlush(any())).thenReturn(collectionExercise);
    SurveyDTO survey = FixtureHelper.loadClassFixtures(SurveyDTO[].class).get(0);
    when(this.surveyService.findSurvey(UUID.fromString(toCreate.getSurveyId()))).thenReturn(survey);
    ActionPlanDTO actionPlanDTO = new ActionPlanDTO();
    actionPlanDTO.setId(UUID.randomUUID());
    when(actionService.createActionPlan(any(), any(), any())).thenReturn(actionPlanDTO);

    // When
    this.collectionExerciseServiceImpl.createCollectionExercise(toCreate, survey);

    // Then check that all actionplans are created in the correct state
    verify(actionService, times(1)).createActionPlan("BRES B", "BRES B Case", null);
    verify(actionService, times(1)).createActionPlan("BRES BI", "BRES BI Case", null);

    String exerciseRef = collectionExercise.getExerciseRef();
    String surveyRef = survey.getSurveyRef();
    HashMap<String, String> overrideBSelectors = new HashMap<>();
    overrideBSelectors.put("surveyRef", surveyRef);
    overrideBSelectors.put("exerciseRef", exerciseRef);
    overrideBSelectors.put("activeEnrolment", "false");
    verify(actionService, times(1))
        .createActionPlan("BRES B 202103", "BRES B Case 202103", overrideBSelectors);

    HashMap<String, String> overrideBISelectors = new HashMap<>();
    overrideBISelectors.put("surveyRef", surveyRef);
    overrideBISelectors.put("exerciseRef", exerciseRef);
    overrideBISelectors.put("activeEnrolment", "true");
    verify(actionService, times(1))
        .createActionPlan("BRES BI 202103", "BRES BI Case 202103", overrideBISelectors);
  }

  /**
   * Tests that creating a collection exercise for which action plans exists does not try to create
   * existing default action plans
   */
  @Test
  public void testCreateCollectionExerciseExistingDefaultActionPlans() throws Exception {
    // Given
    CollectionExerciseDTO toCreate =
        FixtureHelper.loadClassFixtures(CollectionExerciseDTO[].class).get(0);
    CollectionExercise collectionExercise =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    collectionExercise.setExerciseRef(toCreate.getExerciseRef());
    when(collexRepo.saveAndFlush(any())).thenReturn(collectionExercise);
    SurveyDTO survey = FixtureHelper.loadClassFixtures(SurveyDTO[].class).get(0);

    when(this.surveyService.findSurvey(UUID.fromString(toCreate.getSurveyId()))).thenReturn(survey);
    ActionPlanDTO actionPlanDTO = new ActionPlanDTO();
    actionPlanDTO.setId(UUID.randomUUID());
    when(actionService.createActionPlan(any(), any(), any())).thenReturn(actionPlanDTO);
    CaseTypeDefault caseTypedefault = new CaseTypeDefault();
    when(caseTypeDefaultRepo.findTopBySurveyIdAndSampleUnitTypeFK(any(), any()))
        .thenReturn(caseTypedefault);

    // When
    this.collectionExerciseServiceImpl.createCollectionExercise(toCreate, survey);

    // Then
    verify(actionService, times(0)).createActionPlan("BRES B", "BRES B Case", null);
    verify(actionService, times(0)).createActionPlan("BRES BI", "BRES BI Case", null);
  }

  /**
   * Tests that creating a collection exercise for which action plans exists does not try to create
   * existing override action plans
   */
  @Test
  public void testCreateCollectionExerciseExistingOverrideActionPlans() throws Exception {
    // Given
    CollectionExerciseDTO toCreate =
        FixtureHelper.loadClassFixtures(CollectionExerciseDTO[].class).get(0);
    CollectionExercise collectionExercise =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    collectionExercise.setExerciseRef(toCreate.getExerciseRef());
    when(collexRepo.saveAndFlush(any())).thenReturn(collectionExercise);
    SurveyDTO survey = FixtureHelper.loadClassFixtures(SurveyDTO[].class).get(0);

    when(this.surveyService.findSurvey(UUID.fromString(toCreate.getSurveyId()))).thenReturn(survey);
    ActionPlanDTO actionPlanDTO = new ActionPlanDTO();
    actionPlanDTO.setId(UUID.randomUUID());
    when(actionService.createActionPlan(any(), any(), any())).thenReturn(actionPlanDTO);
    CaseTypeOverride caseTypeOverride = new CaseTypeOverride();
    when(caseTypeOverrideRepo.findTopByExerciseFKAndSampleUnitTypeFK(any(), any()))
        .thenReturn(caseTypeOverride);

    // When
    this.collectionExerciseServiceImpl.createCollectionExercise(toCreate, survey);

    // Then
    String exerciseRef = collectionExercise.getExerciseRef();
    String surveyRef = survey.getSurveyRef();
    HashMap<String, String> overrideBSelectors = new HashMap<>();
    overrideBSelectors.put("surveyRef", surveyRef);
    overrideBSelectors.put("exerciseRef", exerciseRef);
    overrideBSelectors.put("activeEnrolment", "false");
    verify(actionService, times(0))
        .createActionPlan("BRES B 202103", "BRES B Case 202103", overrideBSelectors);

    HashMap<String, String> overrideBISelectors = new HashMap<>();
    overrideBISelectors.put("surveyRef", surveyRef);
    overrideBISelectors.put("exerciseRef", exerciseRef);
    overrideBISelectors.put("activeEnrolment", "true");
    verify(actionService, times(0))
        .createActionPlan("BRES BI 202103", "BRES BI Case 202103", overrideBISelectors);
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

    this.collectionExerciseServiceImpl.updateCollectionExercise(existing.getId(), toUpdate);

    ArgumentCaptor<CollectionExercise> captor = ArgumentCaptor.forClass(CollectionExercise.class);

    verify(collexRepo).saveAndFlush(captor.capture());
    CollectionExercise collex = captor.getValue();
    assertEquals(UUID.fromString(toUpdate.getSurveyId()), collex.getSurveyId());
    assertEquals(toUpdate.getExerciseRef(), collex.getExerciseRef());
    assertEquals(toUpdate.getName(), collex.getName());
    assertEquals(toUpdate.getUserDescription(), collex.getUserDescription());
    assertNotNull(collex.getUpdated());
  }

  @Test
  public void testUpdateCollectionExerciseSendsCollectionTransitionEvent() throws Exception {
    // Given
    CollectionExercise existing =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    SurveyDTO survey = FixtureHelper.loadClassFixtures(SurveyDTO[].class).get(0);
    UUID surveyId = UUID.fromString(survey.getId());
    existing.setSurveyId(surveyId);
    when(collexRepo.findOneById(existing.getId())).thenReturn(existing);
    when(surveyService.findSurvey(surveyId)).thenReturn(survey);
    CollectionExerciseDTO toUpdate =
        FixtureHelper.loadClassFixtures(CollectionExerciseDTO[].class).get(0);

    // When
    this.collectionExerciseServiceImpl.updateCollectionExercise(existing.getId(), toUpdate);

    // Then
    CollectionTransitionEvent collectionTransitionEvent =
        new CollectionTransitionEvent(
            existing.getId(), CollectionExerciseDTO.CollectionExerciseState.EXECUTED);
    verify(rabbitTemplate).convertAndSend(collectionTransitionEvent);
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
      this.collectionExerciseServiceImpl.updateCollectionExercise(existing.getId(), toUpdate);
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
      this.collectionExerciseServiceImpl.updateCollectionExercise(existing.getId(), toUpdate);

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
      this.collectionExerciseServiceImpl.updateCollectionExercise(updateUuid, toUpdate);
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

    this.collectionExerciseServiceImpl.deleteCollectionExercise(existing.getId());

    ArgumentCaptor<CollectionExercise> captor = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(this.collexRepo).saveAndFlush(captor.capture());

    assertEquals(true, captor.getValue().getDeleted());
  }

  @Test
  public void testUndeleteCollectionExercise() throws Exception {
    CollectionExercise existing =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class).get(0);
    when(collexRepo.findOneById(existing.getId())).thenReturn(existing);

    this.collectionExerciseServiceImpl.undeleteCollectionExercise(existing.getId());

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
      this.collectionExerciseServiceImpl.patchCollectionExercise(updateUuid, toUpdate);

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
    String exerciseRef = "209966";
    collex.setExerciseRef(exerciseRef);

    this.collectionExerciseServiceImpl.patchCollectionExercise(existing.getId(), collex);

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
    collex.setName(name);

    this.collectionExerciseServiceImpl.patchCollectionExercise(existing.getId(), collex);

    ArgumentCaptor<CollectionExercise> captor = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(this.collexRepo).saveAndFlush(captor.capture());

    CollectionExercise ce = captor.getValue();
    assertEquals(name, ce.getName());
    assertNotNull(ce.getUpdated());
  }

  @Test
  public void testPatchCollectionExerciseUserDescription() throws Exception {
    CollectionExercise existing = setupCollectionExercise();
    CollectionExerciseDTO collex = new CollectionExerciseDTO();
    String userDescription = "Really odd description";
    collex.setUserDescription(userDescription);

    this.collectionExerciseServiceImpl.patchCollectionExercise(existing.getId(), collex);

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
      this.collectionExerciseServiceImpl.patchCollectionExercise(existing.getId(), toUpdate);

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
    collectionExerciseServiceImpl.transitionScheduleCollectionExerciseToReadyToReview(exercise);

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
    collectionExerciseServiceImpl.transitionScheduleCollectionExerciseToReadyToReview(exercise);

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
    collectionExerciseServiceImpl.transitionScheduleCollectionExerciseToReadyToReview(exercise);

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
    collectionExerciseServiceImpl.transitionScheduleCollectionExerciseToReadyToReview(exercise);

    // Then
    exercise.setState(CollectionExerciseDTO.CollectionExerciseState.READY_FOR_REVIEW);
    verify(collexRepo, times(0)).saveAndFlush(exercise);
  }

  @Test
  public void testCreateLink() {
    UUID sampleSummaryUuid = UUID.randomUUID(), collexUuid = UUID.randomUUID();

    when(this.sampleLinkRepository.saveAndFlush(any(SampleLink.class))).then(returnsFirstArg());

    SampleLink sampleLink =
        this.collectionExerciseServiceImpl.createLink(sampleSummaryUuid, collexUuid);

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
    this.collectionExerciseServiceImpl.linkSampleSummaryToCollectionExercise(
        collexUuid, Collections.singletonList(sampleSummaryUuid));

    // Then
    verify(stateManager)
        .transition(
            CollectionExerciseDTO.CollectionExerciseState.CREATED,
            CollectionExerciseDTO.CollectionExerciseEvent.CI_SAMPLE_ADDED);
  }

  public void testRemoveSampleSummaryLink() throws Exception {
    // Given
    final UUID collectionExerciseId = UUID.fromString("3ec82e0e-18ff-4886-8703-5b83442041ba");
    final UUID sampleSummaryId = UUID.fromString("87043936-4d38-4696-952a-fcd55a51be96");
    final List<SampleLink> emptySampleLinks = new ArrayList<>();

    doNothing()
        .when(collectionExerciseServiceImpl)
        .transitionCollectionExercise(collectionExerciseId, CI_SAMPLE_DELETED);
    when(sampleLinkRepository.findByCollectionExerciseId(collectionExerciseId))
        .thenReturn(emptySampleLinks);

    // When
    collectionExerciseServiceImpl.removeSampleSummaryLink(sampleSummaryId, collectionExerciseId);

    // Then
    verify(sampleLinkRepository, times(1))
        .deleteBySampleSummaryIdAndCollectionExerciseId(sampleSummaryId, collectionExerciseId);
    verify(collectionExerciseServiceImpl, times(1))
        .transitionCollectionExercise(collectionExerciseId, CI_SAMPLE_DELETED);
  }
}
