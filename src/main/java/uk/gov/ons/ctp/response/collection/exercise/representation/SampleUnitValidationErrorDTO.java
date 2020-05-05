package uk.gov.ons.ctp.response.collection.exercise.representation;

import lombok.Data;

/** DTO for collection exercise validation errors */
@Data
public class SampleUnitValidationErrorDTO {
  /** An enum outlining the different types of validation errors that can affect sample units */
  public enum ValidationError {
    MISSING_COLLECTION_INSTRUMENT,
    MISSING_PARTY
  }

  private String sampleUnitRef;
  private ValidationError[] errors;
}
