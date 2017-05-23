package uk.gov.ons.ctp.response.collection.exercise.representation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * CollectionExercise API representation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CollectionExerciseDTO {

  private UUID id;

  private Integer exercisePK;

  private String surveyId;

  private String name;

  private Timestamp actualExecutionDateTime;

  private Timestamp scheduledExecutionDateTime;

  private Timestamp scheduledStartDateTime;

  private Timestamp actualPublishDateTime;

  private Timestamp periodStartDateTime;

  private Timestamp periodEndDateTime;

  private Timestamp scheduledReturnDateTime;

  private Timestamp scheduledEndDateTime;

  private String executedBy;

  private CollectionExerciseDTO.CollectionExerciseState state;

  private List<CaseTypeDTO> caseTypes;

  private Integer sampleSize;

  /**
   * enum for collection exercise state
   */
  public enum CollectionExerciseState {
    INIT,
    PENDING,
    EXECUTED,
    VALIDATED,
    FAILEDVALIDATION,
    PUBLISHED;
  }

  /**
   * enum for collection exercise event
   */
  public enum CollectionExerciseEvent {
    REQUEST,
    EXECUTE,
    VALIDATE,
    INVALIDATED,
    PUBLISH
  }

}
