package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.message.rabbit.Rabbitmq;
import uk.gov.ons.ctp.common.message.rabbit.SimpleMessageSender;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.representation.LinkSampleSummaryDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.party.representation.SampleLinkDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SampleUploadedInboundReceiverTest {

    @InjectMocks
    private SampleUploadedInboundReceiver receiver;

    @Mock
    private SampleService sampleService;

    @Mock
    private CollectionExerciseService collectionExerciseService;

    @Mock
    private SimpleMessageSender sender;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private StateTransitionManager<LinkSampleSummaryDTO.SampleLinkState,
            LinkSampleSummaryDTO.SampleLinkEvent> sampleLinkState;

    private SampleLink generateSampleLink(){
        UUID sampleLinkId1 = UUID.randomUUID(),
                collexId1 = UUID.randomUUID();

        SampleLink sampleLink = new SampleLink();
        sampleLink.setCollectionExerciseId(collexId1);
        sampleLink.setSampleSummaryId(sampleLinkId1);

        return sampleLink;
    }

    @Test
    public void testActivateSampleLink() throws CTPException {
        SampleLink sampleLink = generateSampleLink();
        List<SampleLink> sampleLinkList = Arrays.asList(new SampleLink[]{ sampleLink });

        when(this.sampleService.getSampleLinksForSummary(any())).thenReturn(sampleLinkList);
        when(this.sampleLinkState.transition(any(),
                any(LinkSampleSummaryDTO.SampleLinkEvent.class))).thenReturn(LinkSampleSummaryDTO.SampleLinkState.ACTIVE);

        this.receiver.sampleUploaded(new SampleSummaryDTO());

        ArgumentCaptor<SampleLink> sampleLinkCaptor = ArgumentCaptor.forClass(SampleLink.class);
        verify(this.sampleService, times(1)).saveSampleLink(sampleLinkCaptor.capture());
        verify(this.rabbitTemplate, times(1))
                .convertAndSend(
                        eq(SampleUploadedInboundReceiver.OUTBOUND_EXCHANGE),
                        eq(SampleUploadedInboundReceiver.OUTBOUND_ROUTING_KEY),
                        anyString());
        verify(this.collectionExerciseService, times(1))
                .transitionScheduleCollectionExerciseToReadyToReview(eq(sampleLink.getCollectionExerciseId()));

        SampleLink capturedLink = sampleLinkCaptor.getValue();

        assertEquals(sampleLink.getSampleSummaryId(), capturedLink.getSampleSummaryId());
        assertEquals(sampleLink.getCollectionExerciseId(), capturedLink.getCollectionExerciseId());
        assertEquals(LinkSampleSummaryDTO.SampleLinkState.ACTIVE, capturedLink.getState());
    }
}
