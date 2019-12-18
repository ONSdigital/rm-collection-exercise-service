package uk.gov.ons.ctp.response.collection.exercise.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;

public interface EventRepository extends JpaRepository<Event, Integer> {
  Event findOneById(UUID id);

  List<Event> findByCollectionExercise(CollectionExercise collex);

  Event findOneByCollectionExerciseAndTag(CollectionExercise collex, String tag);

  List<Event> findByCollectionExerciseId(UUID collexId);

  List<Event> findByCollectionExerciseId(List<UUID> collexId);

  /**
   * Find events where elapsed message has already been sent
   *
   * @return the list of matching events
   */
  List<Event> findByMessageSentNotNull();

  /**
   * Find events where elapsed message has not been sent
   *
   * @return the list of matching events
   */
  List<Event> findByMessageSentNull();
}
