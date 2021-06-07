package uk.gov.ons.ctp.response.collection.exercise.lib.sampleunit.definition;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SampleAttributes {
  protected List<Entry> entries;
}
