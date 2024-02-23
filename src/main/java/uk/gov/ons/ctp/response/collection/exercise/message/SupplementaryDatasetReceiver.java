package uk.gov.ons.ctp.response.collection.exercise.message;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.SupplementaryDatasetDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.SupplementaryDatasetService;

@Component
public class SupplementaryDatasetReceiver {
  private static final Logger log = LoggerFactory.getLogger(SupplementaryDatasetReceiver.class);

  @Autowired private ObjectMapper objectMapper;

  @Autowired private SupplementaryDatasetService supplementaryDatasetService;

  @ServiceActivator(inputChannel = "supplementaryDataServiceMessageChannel")
  public void messageReceiver(
      Message message,
      @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage pubSubMsg)
      throws CTPException {
    log.info(
        "Receiving message ID from PubSub {}",
        kv("messageId", pubSubMsg.getPubsubMessage().getMessageId()));
    String payload = new String((byte[]) message.getPayload());
    log.with("payload", payload).info("New message from Supplementary Data Service");

    try {
      log.info("Mapping payload to Supplementary Dataset object");
      SupplementaryDatasetDTO supplementaryDatasetDTO = createSupplementaryDatasetDTO(payload);
      supplementaryDatasetService.addSupplementaryDatasetEntity(supplementaryDatasetDTO);
      pubSubMsg.ack();
    } catch (CTPException e) {
      String surveyId = null;
      String periodId = null;
      try {
        surveyId = getSurveyId(payload);
        periodId = getPeriodId(payload);
      } catch (CTPException ex) {
        log.error("Error extracting surveyRef or period from message: {}", ex.getMessage());
      }
      log.error(
          "Error processing message from Supplementary Dataset Service for collection exercise period Id {} and surveyId {}: {}",
          periodId,
          surveyId,
          e.getMessage());
      pubSubMsg.nack();

      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format(
              "Cannot find collection exercise for surveyRef=%s and period=%s",
              surveyId, periodId));
    }
  }

  private String getSurveyId(String payload) throws CTPException {
    try {
      return objectMapper.readTree(payload).get("survey_id").asText();
    } catch (JsonProcessingException e) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST, "Could not extract survey_id from message");
    }
  }

  private String getPeriodId(String payload) throws CTPException {
    try {
      return objectMapper.readTree(payload).get("period_id").asText();
    } catch (JsonProcessingException e) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST, "Could not extract period_id from message");
    }
  }

  private SupplementaryDatasetDTO createSupplementaryDatasetDTO(String payload)
      throws CTPException {
    SupplementaryDatasetDTO supplementaryDatasetDTO;
    try {
      supplementaryDatasetDTO = objectMapper.readValue(payload, SupplementaryDatasetDTO.class);
    } catch (JsonProcessingException e) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST, "Could not map message to Supplementary Dataset DTO");
    }
    log.info("Mapping to Supplementary Dataset object successful {}", supplementaryDatasetDTO);
    return supplementaryDatasetDTO;
  }
}
