package uk.gov.ons.ctp.response.collection.exercise.representation;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SampleUnitGroup API representation.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SampleUnitGroupDTO {

  /**
   * enum for survey unit group state
   */
  public enum SampleUnitGroupState {
    INIT,
    VALIDATED,
    PUBLISHED;
  }

  /**
   * enum for survey unit group event
   */
  public enum SampleUnitGroupEvent {
    VALIDATE,
    PUBLISH
  }

  private Integer sampleUnitGroupId;
}
