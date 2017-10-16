package uk.gov.ons.ctp.response.collection.exercise.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;

/**
 * JPA Data Repository.
 */
public interface SampleLinkRepository extends JpaRepository<SampleLink, Integer> {

  /**
   * delete sample summaries linked to collection exercise
   *
   * @param id the UUID of the collection exercise to delete linked Sample summaries for
   */
  void deleteByCollectionExerciseId(UUID id);

  /**
   * find sample summaries linked to collection exercise
   *
   * @param id UUID for collection exercise
   * @return list of SampleLink showing which sample summaries are linked to a collection exercise
   */
  List<SampleLink> findByCollectionExerciseId(UUID id);

}
