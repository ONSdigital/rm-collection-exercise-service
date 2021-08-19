package uk.gov.ons.ctp.response.collection.exercise.representation;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model object */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SampleSummaryActivationDTO {
  private UUID collectionExerciseId;
  private UUID sampleSummaryId;
  private UUID surveyId;
}
