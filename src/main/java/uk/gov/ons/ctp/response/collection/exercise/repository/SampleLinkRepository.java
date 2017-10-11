package uk.gov.ons.ctp.response.collection.exercise.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;

public interface SampleLinkRepository extends JpaRepository<SampleLink, Integer>{
  
  void deleteByCollectionExerciseId(UUID id);

}
