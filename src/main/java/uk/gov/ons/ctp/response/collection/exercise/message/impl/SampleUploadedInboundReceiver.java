package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.representation.LinkSampleSummaryDTO.SampleLinkState;
import uk.gov.ons.ctp.response.collection.exercise.representation.LinkSampleSummaryDTO.SampleLinkEvent;
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
    @Qualifier("sampleLink")
    private StateTransitionManager<SampleLinkState, SampleLinkEvent>
            sampleLinkState;

    private void activateSampleLink(SampleLink sampleLink){
        try {
            SampleLinkState newState =
                    this.sampleLinkState.transition(sampleLink.getState(),
                            SampleLinkEvent.ACTIVATE);

            sampleLink.setState(newState);

            this.sampleService.saveSampleLink(sampleLink);

            this.collectionExerciseService.transitionScheduleCollectionExerciseToReadyToReview(
                    sampleLink.getCollectionExerciseId());
        } catch (CTPException e) {
            log.error("Failed to activate sample link {} - {}", sampleLink, e);
        }
    }

    /**
     * Service activator method - check we know about the sample link and set it to active
     *
     * @param sampleSummary the sample summary for which the upload has completed
     */
    @ServiceActivator(inputChannel = "sampleSummaryInMessage")
    public void sampleUploaded(final SampleSummaryDTO sampleSummary){
            List<SampleLink> links = this.sampleService.getSampleLinksForSummary(sampleSummary.getId());

            links.stream().forEach(l -> activateSampleLink(l));
    }
}

