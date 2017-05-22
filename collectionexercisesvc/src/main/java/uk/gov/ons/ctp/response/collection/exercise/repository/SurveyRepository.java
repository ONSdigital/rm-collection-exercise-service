package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;

/**
 * Spring JPA Repository for Survey
 *
 */
public interface SurveyRepository extends JpaRepository<Survey, String> {

    Survey findBySurveyId(String surveyId);

}
