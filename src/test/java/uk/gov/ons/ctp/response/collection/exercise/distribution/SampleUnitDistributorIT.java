package uk.gov.ons.ctp.response.collection.exercise.distribution;

import static org.junit.Assert.assertEquals;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeDefault;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeDefaultRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeOverrideRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;

@Slf4j
@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SampleUnitDistributorIT {
  // Gubbins to make spring wire itself up
  @ClassRule public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
  @Rule public final SpringMethodRule springMethodRule = new SpringMethodRule();

  // Under test
  @Autowired private SampleUnitDistributor sampleUnitDistributor;

  // Repos that we need
  @Autowired private CollectionExerciseRepository collexRepo;
  @Autowired private CaseTypeDefaultRepository caseTypeDefaultRepo;
  @Autowired private CaseTypeOverrideRepository caseTypeOverrideRepo;

  @Test
  public void testGetActiveActionPlanIdOverride() {
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(UUID.randomUUID());
    collectionExercise.setState(CollectionExerciseState.CREATED);
    collectionExercise.setExerciseRef("ZZZ666");
    collectionExercise = collexRepo.saveAndFlush(collectionExercise);

    UUID expectedActionPlanId = UUID.randomUUID();
    CaseTypeOverride caseTypeOverride = new CaseTypeOverride();
    caseTypeOverride.setActionPlanId(expectedActionPlanId);
    caseTypeOverride.setExerciseFK(collectionExercise.getExercisePK());
    caseTypeOverride.setSampleUnitTypeFK("B");
    caseTypeOverride = caseTypeOverrideRepo.saveAndFlush(caseTypeOverride);

    String actualActionPlanId =
        sampleUnitDistributor.getActiveActionPlanId(
            collectionExercise.getExercisePK(), "B", UUID.randomUUID());

    assertEquals(expectedActionPlanId.toString(), actualActionPlanId);

    // Teardown
    caseTypeOverrideRepo.delete(caseTypeOverride);
    collexRepo.delete(collectionExercise);
  }

  @Test
  public void testGetActiveActionPlanIdDefault() {
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(UUID.randomUUID());
    collectionExercise.setState(CollectionExerciseState.CREATED);
    collectionExercise.setExerciseRef("ZZZ666");
    collectionExercise = collexRepo.saveAndFlush(collectionExercise);

    UUID expectedActionPlanId = UUID.randomUUID();
    UUID surveyId = UUID.randomUUID();
    CaseTypeDefault caseTypeDefault = new CaseTypeDefault();
    caseTypeDefault.setActionPlanId(expectedActionPlanId);
    caseTypeDefault.setSampleUnitTypeFK("B");
    caseTypeDefault.setSurveyId(surveyId);
    caseTypeDefault = caseTypeDefaultRepo.saveAndFlush(caseTypeDefault);

    String actualActionPlanId =
        sampleUnitDistributor.getActiveActionPlanId(
            collectionExercise.getExercisePK(), "B", surveyId);

    assertEquals(expectedActionPlanId.toString(), actualActionPlanId);

    // Teardown
    caseTypeDefaultRepo.delete(caseTypeDefault);
    collexRepo.delete(collectionExercise);
  }
}
