package uk.gov.ons.ctp.response.collection.exercise.service;

import java.util.List;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;

/** Service responsible for dealing with stored ExerciseSampleUnits. */
public interface ExerciseSampleUnitService {

  /**
   * Query repository for SampleUnits belonging to a SampleUnitGroup.
   *
   * @param sampleUnitGroup to which the SampleUnits belong.
   * @return List<ExerciseSampleUnit> SampleUnits belonging to SampleUnitGroup
   */
  List<ExerciseSampleUnit> findBySampleUnitGroup(ExerciseSampleUnitGroup sampleUnitGroup);
}
