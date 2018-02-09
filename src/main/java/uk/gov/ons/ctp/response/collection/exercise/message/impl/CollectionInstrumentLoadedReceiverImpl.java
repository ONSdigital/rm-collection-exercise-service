package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.CollectionInstrumentMessageDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;

import java.util.UUID;

import static uk.gov.ons.ctp.response.collection.exercise.message.impl.CollectionInstrumentLoadedReceiverImpl.Action.ADD;

@MessageEndpoint
@Slf4j
public class CollectionInstrumentLoadedReceiverImpl {

    @Autowired
    private CollectionExerciseService collectionExerciseService;

    enum Action {
        ADD, REMOVE
    }

    private Action parseAction(String actionStr){
        if (StringUtils.isBlank(actionStr)){
            return ADD;
        } else {
            return Action.valueOf(actionStr.toUpperCase());
        }
    }

    @ServiceActivator(inputChannel = "ciMessageDto")
    public void acceptSampleUnit(CollectionInstrumentMessageDTO message) throws CTPException {
        log.info("json: {}", message);

        UUID collexId = message.getExerciseId();
        Action action = parseAction(message.getAction());

        switch(action){
            case ADD:
                this.collectionExerciseService.incrementCollectionInstrumentReferenceCount(collexId);
                break;
            case REMOVE:
                this.collectionExerciseService.decrementCollectionInstrumentReferenceCount(collexId);
                break;
        }

        this.collectionExerciseService.maybeSendCiSampleAdded(collexId);
    }
}
