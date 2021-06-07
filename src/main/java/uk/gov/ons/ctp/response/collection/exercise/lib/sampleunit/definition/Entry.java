package uk.gov.ons.ctp.response.collection.exercise.lib.sampleunit.definition;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Entry {
  protected String key;
  protected String value;
}
