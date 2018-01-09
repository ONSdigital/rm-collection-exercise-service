package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, Integer> {
    Event findOneById(UUID id);
    List<Event> findByCollectionExercise(CollectionExercise collex);
    Event findOneByCollectionExerciseAndTag(CollectionExercise collex, String tag);
    List<Event> findByCollectionExerciseId(UUID collexId);
}
