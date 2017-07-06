package uk.gov.ons.ctp.response.collection.exercise.representation;

import java.util.Date;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CollectionExerciseSummary API representation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CollectionExerciseSummaryDTO {

  private UUID id;

  private String name;

  private Date scheduledExecutionDateTime;

}
