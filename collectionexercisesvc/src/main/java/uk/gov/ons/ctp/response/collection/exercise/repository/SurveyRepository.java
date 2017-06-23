package uk.gov.ons.ctp.response.collection.exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;

import java.util.UUID;

/**
 * Spring JPA Repository for Survey
 *
 */
public interface SurveyRepository extends JpaRepository<Survey, Integer> {

    /**
     * Query repository for survey by id.
     *
     * @param id survey id to find.
     * @return Survey object.
     */
    Survey findById(UUID id);

}
