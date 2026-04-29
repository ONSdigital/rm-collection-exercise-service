package uk.gov.ons.ctp.response.collection.exercise.message.dto;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CollectionExerciseEndEventDTO {
  private UUID collectionExerciseId;
  private UUID SupplementaryDatasetId;
  private String period;
  private String surveyRef;
  private Timestamp endDate;
}
