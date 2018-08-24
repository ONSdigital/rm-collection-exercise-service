package uk.gov.ons.ctp.response.collection.exercise.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;

/** Implementation to deal with sampleUnits. */
@Service
public class ExerciseSampleUnitService {

  @Autowired private SampleUnitRepository sampleUnitRepo;

  /**
   * Query repository for SampleUnits belonging to a SampleUnitGroup.
   *
   * @param sampleUnitGroup to which the SampleUnits belong.
   * @return List<ExerciseSampleUnit> SampleUnits belonging to SampleUnitGroup
   */
  public List<ExerciseSampleUnit> findBySampleUnitGroup(ExerciseSampleUnitGroup sampleUnitGroup) {
    return sampleUnitRepo.findBySampleUnitGroup(sampleUnitGroup);
  }
}
