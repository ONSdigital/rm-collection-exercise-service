package uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation;

import static uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleSummaryDTO.ErrorCode.None;

import java.util.Date;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model object */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SampleSummaryDTO {

  /** enum for Sample state */
  public enum SampleState {
    ACTIVE,
    INIT,
    FAILED
  }

  /** enum for Sample event */
  public enum SampleEvent {
    ACTIVATED,
    FAIL_VALIDATION
  }

  /**
   * Enum to indicate error codes for sample uploads None - no error NotCsv - a sample file is not a
   * csv DataError - a sample file contains a data error NotSpecified - an error has occurred but
   * it's nature cannot be ascertained (e.g. generic Exception)
   */
  public enum ErrorCode {
    None,
    NotCsv,
    DataError,
    NotSpecified
  }

  private UUID id;

  private Date effectiveStartDateTime;

  private Date effectiveEndDateTime;

  private String surveyRef;

  private Date ingestDateTime;

  private SampleState state;

  private Integer totalSampleUnits;

  private Integer expectedCollectionInstruments;

  private String notes;

  private ErrorCode errorCode = None;
}
