package uk.gov.ons.ctp.response.collection.exercise.lib.collection.instrument.representation;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model object */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ClassifiersDTO {
  private List<String> COLLECTION_EXERCISE;
  private List<String> RU_REF;
  private String eq_id;
  private String form_type;
}
