package uk.gov.ons.ctp.response.collection.exercise.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.CollectionExerciseApplication.CollectionExerciseEndOutboundGateway;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.lib.survey.representation.SurveyDTO;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.CollectionExerciseEndEventDTO;

@Component
public class CollectionExerciseEndPublisher {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(CollectionExerciseEndPublisher.class);

  @Autowired private SurveySvcClient surveyService;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private CollectionExerciseEndOutboundGateway messagingGateway;

  public void sendCollectionExerciseEnd(CollectionExercise collectionExercise) {

    SurveyDTO survey = this.surveyService.findSurvey(collectionExercise.getSurveyId());

    CollectionExerciseEndEventDTO collectionExerciseEndEventDTO =
        new CollectionExerciseEndEventDTO();
    collectionExerciseEndEventDTO.setCollectionExerciseId(collectionExercise.getId());
    collectionExerciseEndEventDTO.setPeriod(collectionExercise.getExerciseRef());
    collectionExerciseEndEventDTO.setSurveyRef(survey.getId());
    collectionExerciseEndEventDTO.setSupplementaryDatasetId(
        collectionExercise.getSupplementaryDatasetEntity().getSupplementaryDatasetId());
    collectionExerciseEndEventDTO.setEndDate(collectionExercise.getScheduledEndDateTime());

    try {
      String payload = objectMapper.writeValueAsString(collectionExerciseEndEventDTO);
      messagingGateway.sendToPubsub(payload);
    } catch (JsonProcessingException e) {
      LOGGER.error("Failed to serialise collection exercise end event", e);
    }
  }
}
