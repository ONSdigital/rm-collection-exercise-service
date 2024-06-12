package uk.gov.ons.ctp.response.collection.exercise.representation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.domain.SupplementaryDatasetEntity;

/** CollectionExercise API representation. */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CollectionExerciseDTO {

  private UUID id;

  @NotNull(groups = {PutValidation.class})
  private String surveyId;

  // When creating a collection exercise (PostValidation), the survey can either
  // be specified by the
  // surveyId or
  // surveyRef. This field will not be set when returning collection exercises
  // (input field only)
  private String surveyRef;

  @Size(
      max = 20,
      min = 1,
      groups = {PostValidation.class, PutValidation.class, PatchValidation.class})
  private String name;

  private Date actualExecutionDateTime;

  private Date scheduledExecutionDateTime;

  private Date scheduledStartDateTime;

  private Date actualPublishDateTime;

  private Date periodStartDateTime;

  private Date periodEndDateTime;

  private Date scheduledReturnDateTime;

  private Date scheduledEndDateTime;

  private String executedBy;

  private CollectionExerciseDTO.CollectionExerciseState state;

  @NotNull(groups = {PostValidation.class, PutValidation.class})
  @Pattern(
      regexp = "^[0-9]{1,6}$",
      groups = {PostValidation.class, PutValidation.class, PatchValidation.class})
  private String exerciseRef;

  @Size(
      max = 50,
      min = 1,
      groups = {PostValidation.class, PutValidation.class, PatchValidation.class})
  private String userDescription;

  private Date created;

  private Date updated;

  private Boolean deleted;

  private SampleUnitValidationErrorDTO[] validationErrors;

  private List<EventDTO> events;

  private Integer sampleSize;

  private List<SampleLink> sampleLinks;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private SupplementaryDatasetEntity supplementaryDatasetEntity;

  /** Empty interface to use as a marker for validation of POST requests */
  public interface PostValidation {}

  /** Empty interface to use as a marker for validation of PUT requests */
  public interface PutValidation {}

  /** Empty interface to use as a marker for validation of PUT sub-resource requests */
  public interface PatchValidation {}

  /** enum for collection exercise state */
  public enum CollectionExerciseState {
    CREATED,
    SCHEDULED,
    READY_FOR_REVIEW,
    READY_FOR_LIVE,
    EXECUTION_STARTED,
    EXECUTED,
    VALIDATED,
    FAILEDVALIDATION,
    LIVE,
    ENDED
  }

  /** enum for collection exercise event */
  public enum CollectionExerciseEvent {
    EVENTS_ADDED,
    EVENTS_DELETED,
    CI_SAMPLE_ADDED,
    CI_SAMPLE_DELETED,
    REVIEWED,
    EXECUTION_COMPLETE,
    EXECUTE,
    VALIDATE,
    INVALIDATE,
    PUBLISH,
    GO_LIVE,
    END_EXERCISE
  }

  @JsonIgnore
  public String getSurveyRef() {
    return surveyRef;
  }

  @JsonProperty
  public void setSurveyRef(String surveyRef) {
    this.surveyRef = surveyRef;
  }
}
