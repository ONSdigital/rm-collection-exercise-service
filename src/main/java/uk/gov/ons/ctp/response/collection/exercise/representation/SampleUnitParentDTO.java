package uk.gov.ons.ctp.response.collection.exercise.representation;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class SampleUnitParentDTO extends SampleUnit {
  protected String collectionExerciseId;
  protected SampleUnitChildrenDTO sampleUnitChildrenDTO;
}
