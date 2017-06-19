package uk.gov.ons.ctp.response.collectionInstrument.representation;

import java.util.Map;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model object
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CollectionInstrumentDTO {
  private UUID id;
  private UUID surveyId;
  private Map<String, String> classifiers;

}
