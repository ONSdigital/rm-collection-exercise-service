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
   * delete sample summaries linked to collection exercise
   *
   * @param sampleSummaryId the UUID of the sample to delete link for
   * @param collectionExerciseId the UUID of the collection exercise to delete link for
   */
  void deleteBySampleSummaryIdAndCollectionExerciseId(UUID sampleSummaryId, UUID collectionExerciseId);

  /**
   * find sample summaries linked to collection exercise
   *
   * @param id UUID for collection exercise
   * @return list of SampleLink showing which sample summaries are linked to a collection exercise
   */
  List<SampleLink> findByCollectionExerciseId(UUID id);

  /**
   * Find sample link for a sample summary
   *
   * @param sampleSummaryId the id of the sample summary to find the links for
   * @return a list of SampleLinks with the given sample summary
   */
  List<SampleLink> findBySampleSummaryId(UUID sampleSummaryId);
}
