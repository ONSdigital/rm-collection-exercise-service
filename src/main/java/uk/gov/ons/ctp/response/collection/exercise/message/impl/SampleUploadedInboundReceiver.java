package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.message.rabbit.Rabbitmq;
import uk.gov.ons.ctp.common.message.rabbit.SimpleMessageSender;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.representation.LinkSampleSummaryDTO.SampleLinkEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.LinkSampleSummaryDTO.SampleLinkState;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;

import java.util.List;

/**
 * Class to hold service activator method to handle incoming sample uploaded messages
 */
@MessageEndpoint
@Slf4j
public class SampleUploadedInboundReceiver {

    @Autowired
    private CollectionExerciseService collectionExerciseService;

    @Autowired
    private SampleService sampleService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private SimpleMessageSender sender;

    @Autowired
    @Qualifier("sampleLink")
    private StateTransitionManager<SampleLinkState, SampleLinkEvent>
            sampleLinkState;

    private static String OUTBOUND_EXCHANGE = "collection-outbound-exchange";
    private static String OUTBOUND_ROUTING_KEY = "SampleLink.Activated.binding";

    /**
     * Method to set the state of a SampleLink to ACTIVATED
     * @param sampleLink the SampleLink to change
     */
    private void activateSampleLink(final SampleLink sampleLink) {
        try {
            SampleLinkState newState =
                    this.sampleLinkState.transition(sampleLink.getState(),
                            SampleLinkEvent.ACTIVATE);

            sampleLink.setState(newState);

            this.sampleService.saveSampleLink(sampleLink);

            String message = mapper.writeValueAsString(sampleLink);
            this.sender.sendMessage(OUTBOUND_EXCHANGE, OUTBOUND_ROUTING_KEY, message);
            log.info("Send message {} to {} ({})", message, OUTBOUND_EXCHANGE, OUTBOUND_ROUTING_KEY);

            this.collectionExerciseService.transitionScheduleCollectionExerciseToReadyToReview(
                    sampleLink.getCollectionExerciseId());
        } catch (CTPException e) {
            log.error("Failed to activate sample link {} - {}", sampleLink, e);
        } catch (JsonProcessingException e) {
            log.error("Failed to marshal outgoing activation message {} - {}", sampleLink, e);
        }
    }

    /**
     * Service activator method - check we know about the sample link and set it to active
     *
     * @param sampleSummary the sample summary for which the upload has completed
     */
    @ServiceActivator(inputChannel = "sampleUploadedSampleSummaryInMessage")
    public void sampleUploaded(final SampleSummaryDTO sampleSummary) {
        List<SampleLink> links = this.sampleService.getSampleLinksForSummary(sampleSummary.getId());

        links.stream().forEach(l -> activateSampleLink(l));
    }
}

