package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.CTPException.Fault;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeOverrideRepository;

@RunWith(MockitoJUnitRunner.class)
public class CaseTypeOverrideServiceImplTest {

  private static final String BUSINESS_SAMPLE_UNIT_TYPE = "B";
  private static final int COLLECTION_EXERCISE_PK = 10;
  private static final UUID COLLECTION_EXERCISE_ID =
      UUID.fromString("65dec540-f562-4fbf-87e7-a6ef411101d4");

  @Mock CaseTypeOverrideRepository caseTypeOverrideRepository;

  @Mock Logger logger;

  @InjectMocks CaseTypeOverrideServiceImpl caseTypeOverrideService;

  @Test
  public void testGetCaseTypeOverrideReturnsCaseTypeOverrideOnSuccess() throws CTPException {
    final CaseTypeOverride caseTypeOverride = new CaseTypeOverride();
    when(caseTypeOverrideRepository.findTopByExerciseFKAndSampleUnitTypeFK(
            COLLECTION_EXERCISE_PK, BUSINESS_SAMPLE_UNIT_TYPE))
        .thenReturn(caseTypeOverride);

    final CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setExercisePK(COLLECTION_EXERCISE_PK);

    assertThat(
        caseTypeOverrideService.getCaseTypeOverride(collectionExercise, BUSINESS_SAMPLE_UNIT_TYPE),
        is(caseTypeOverride));
  }

  @Test
  public void testGetCaseTypeOverrideThrowsExceptionOnNotFound() {
    when(caseTypeOverrideRepository.findTopByExerciseFKAndSampleUnitTypeFK(
            COLLECTION_EXERCISE_PK, BUSINESS_SAMPLE_UNIT_TYPE))
        .thenReturn(null);

    final CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(COLLECTION_EXERCISE_ID);
    collectionExercise.setExercisePK(COLLECTION_EXERCISE_PK);

    try {
      caseTypeOverrideService.getCaseTypeOverride(collectionExercise, BUSINESS_SAMPLE_UNIT_TYPE);
    } catch (final CTPException e) {
      assertThat(e.getFault(), is(Fault.RESOURCE_NOT_FOUND));
      assertThat(
          e.getMessage(),
          is(
              "Override action plans do not exist for collection exercise "
                  + COLLECTION_EXERCISE_ID));
    }
  }
}
