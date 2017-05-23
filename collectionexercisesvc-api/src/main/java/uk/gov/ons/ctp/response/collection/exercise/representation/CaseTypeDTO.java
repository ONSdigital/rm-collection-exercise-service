package uk.gov.ons.ctp.response.collection.exercise.representation;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CaseType API representation.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CaseTypeDTO {

  String sampleUnitType;

  String actionPlanId;

}
