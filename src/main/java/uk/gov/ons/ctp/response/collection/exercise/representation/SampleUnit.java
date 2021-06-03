package uk.gov.ons.ctp.response.collection.exercise.representation;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class SampleUnit {
  protected String id;
  @NotNull protected String sampleUnitRef;
  @NotNull protected String sampleUnitType;
  protected String partyId;
  @NotNull protected String collectionInstrumentId;
  protected boolean activeEnrolment;
}
