package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;

import java.util.UUID;

/**
 * Spring JPA Repository for Survey
 *
 */
public interface SurveyRepository extends JpaRepository<Survey, String> {

    Survey findById(UUID id);

}
