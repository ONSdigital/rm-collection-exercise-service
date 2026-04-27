package uk.gov.ons.ctp.response.collection.exercise.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;
import uk.gov.ons.ctp.response.collection.exercise.CollectionExerciseApplication.CollectionExerciseEndOutboundGateway;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyDTO;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.CollectionExerciseEndEventDTO;

@Component
public class CollectionExerciseEndPublisher {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(CollectionExerciseEndPublisher.class);

  @Autowired private SurveySvcClient surveyService;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private CollectionExerciseEndOutboundGateway messagingGateway;

  public void sendCollectionExerciseEnd(UUID collectionExerciseId, UUID supplementaryDatasetId, String exerciseRef, Timestamp endDate, UUID surveyId) {

    SurveyDTO survey = this.surveyService.findSurvey(surveyId);

    CollectionExerciseEndEventDTO collectionExerciseEndEventDTO =
        new CollectionExerciseEndEventDTO();
    collectionExerciseEndEventDTO.setCollectionExerciseId(collectionExerciseId);
    collectionExerciseEndEventDTO.setPeriod(exerciseRef);
    collectionExerciseEndEventDTO.setSurveyRef(survey.getId());
    collectionExerciseEndEventDTO.setSupplementaryDatasetId(supplementaryDatasetId);
    collectionExerciseEndEventDTO.setEndDate(endDate);

    try {
      String payload = objectMapper.writeValueAsString(collectionExerciseEndEventDTO);
      messagingGateway.sendToPubsub(payload);
    } catch (JsonProcessingException e) {
      LOGGER.error("Failed to serialise collection exercise end event", e);
    }
  }
}
