package uk.gov.ons.ctp.response.collection.instrument.representation;

import java.util.HashMap;
import java.util.List;
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
  private String surveyId;
  private Map<String, List<String>> classifiers = new HashMap<>();

}
