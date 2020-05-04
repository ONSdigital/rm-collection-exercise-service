package uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation;

import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model object */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CollectionExerciseJobCreationRequestDTO {

  @NotNull
  @ApiModelProperty(required = true)
  private UUID collectionExerciseId;

  @NotNull
  @ApiModelProperty(required = true)
  private String surveyRef;

  @NotNull
  @ApiModelProperty(required = true)
  private Date exerciseDateTime;

  @NotNull
  @ApiModelProperty(required = true)
  private List<UUID> sampleSummaryUUIDList;
}
