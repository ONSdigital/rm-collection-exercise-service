package uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation;

import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/** SurveyClassifierType API representation */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SurveyClassifierTypeDTO {

  private String id;
  private String name;
  private List<String> classifierTypes;
}
