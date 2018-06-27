package uk.gov.ons.ctp.response.collection.exercise.validation;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;

/** Test ClassifierTypes expected from Survey service */
@RunWith(MockitoJUnitRunner.class)
public class CollectionInstrumentClassifierTypesTest {

  private static final String COLLECTION_EXERCISE = "COLLECTION_EXERCISE";
  private static final String RU_REF = "RU_REF";
  private static final String FORM_TYPE = "FORM_TYPE";

  /**
   * Test have correct classifierTypes and value is set correctly.
   *
   * @throws Exception loading test objects
   */
  @Test
  public void testApply() throws Exception {

    List<ExerciseSampleUnit> sampleUnits =
        FixtureHelper.loadClassFixtures(ExerciseSampleUnit[].class);
    ExerciseSampleUnit sampleUnit = sampleUnits.get(0);
    CollectionInstrumentClassifierTypes classifierTypeCollectionExercise =
        CollectionInstrumentClassifierTypes.valueOf(COLLECTION_EXERCISE);
    assertEquals(
        "14fb3e68-4dca-46db-bf49-04b84e07e77c", classifierTypeCollectionExercise.apply(sampleUnit));

    CollectionInstrumentClassifierTypes classifierTypeSampleUnitRef =
        CollectionInstrumentClassifierTypes.valueOf(RU_REF);
    assertEquals("50000065975", classifierTypeSampleUnitRef.apply(sampleUnit));

    CollectionInstrumentClassifierTypes classifierTypeFormType =
        CollectionInstrumentClassifierTypes.valueOf(FORM_TYPE);
    assertEquals("0015", classifierTypeFormType.apply(sampleUnit));
  }
}
