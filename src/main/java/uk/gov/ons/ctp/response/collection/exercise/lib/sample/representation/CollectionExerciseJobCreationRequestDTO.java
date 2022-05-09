package uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation;

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
  private UUID collectionExerciseId;

  @NotNull
  private String surveyRef;

  @NotNull
  private Date exerciseDateTime;

  @NotNull
  private List<UUID> sampleSummaryUUIDList;
}
