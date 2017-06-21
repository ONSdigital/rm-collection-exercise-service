package uk.gov.ons.ctp.response.collection.exercise.representation;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.UUID;

/**
 * CaseType API representation.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class PartyDTO {

  String sampleUnitType;

  String businessRef;

  UUID id;

  HashMap<String, String> attributes;

}
