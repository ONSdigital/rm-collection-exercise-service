package uk.gov.ons.ctp.response.collection.exercise.representation;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CollectionExercise API representation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CollectionExerciseDTO {

  private UUID id;

  private String surveyId;

  private String name;

  private Date actualExecutionDateTime;

  private Date scheduledExecutionDateTime;

  private Date scheduledStartDateTime;

  private Date actualPublishDateTime;

  private Date periodStartDateTime;

  private Date periodEndDateTime;

  private Date scheduledReturnDateTime;

  private Date scheduledEndDateTime;

  private String executedBy;

  private CollectionExerciseDTO.CollectionExerciseState state;

  private List<CaseTypeDTO> caseTypes;

  /**
   * enum for collection exercise state
   */
  public enum CollectionExerciseState {
    INIT,
    PENDING,
    EXECUTED,
    VALIDATED,
    FAILEDVALIDATION,
    PUBLISHED
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
