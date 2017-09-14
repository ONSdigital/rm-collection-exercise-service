package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.service.ExerciseSampleUnitService;

/**
 * Implementation to deal with sampleUnits.
 */
@Service
public class ExerciseSampleUnitServiceImpl implements ExerciseSampleUnitService {

  @Autowired
  private SampleUnitRepository sampleUnitRepo;

  @Override
  public List<ExerciseSampleUnit> findBySampleUnitGroup(ExerciseSampleUnitGroup sampleUnitGroup) {
    return sampleUnitRepo.findBySampleUnitGroup(sampleUnitGroup);
  }
}
