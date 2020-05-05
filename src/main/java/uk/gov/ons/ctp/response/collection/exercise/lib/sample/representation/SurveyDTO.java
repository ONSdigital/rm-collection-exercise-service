package uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Survey API representation */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SurveyDTO {

  /**
   * This enum denotes the type of the survey. Business and Social are currently actively used
   * throughout. Census is supported as a survey type in the loosest sense in some areas of the
   * system but not in others. Hence it may be deprecated when the real census comes around.
   */
  public enum SurveyType {
    Business,
    Social,
    Census
  }

  private String id;
  private String shortName;
  private String longName;
  private String surveyRef;
  private String legalBasis;
  private String legalBasisRef;
  private SurveyType surveyType;
}
