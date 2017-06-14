package uk.gov.ons.response.survey.representation;

import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SurveyClassifierType API representation
 *
 */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SurveyClassifierTypeDTO {

  private String id;
  private String name;
  private List<String> classifierTypes;

}
