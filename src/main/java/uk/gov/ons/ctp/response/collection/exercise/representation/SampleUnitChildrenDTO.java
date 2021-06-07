package uk.gov.ons.ctp.response.collection.exercise.representation;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.ons.ctp.response.collection.exercise.lib.casesvc.message.sampleunitnotification.SampleUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SampleUnitChildrenDTO {
  protected List<SampleUnit> sampleUnitChildren;
}
