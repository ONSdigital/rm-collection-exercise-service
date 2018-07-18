package uk.gov.ons.ctp.response.collection.exercise.service;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionTransitionEvent {

  private UUID collectionExerciseId;
  private CollectionExerciseDTO.CollectionExerciseState state;
}
