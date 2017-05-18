package uk.gov.ons.ctp.response.collection.exercise.representation;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CollectionExercise API representation.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CollectionExerciseDTO {

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

  private Integer exerciseId;

}
