package uk.gov.ons.ctp.response.collection.exercise.message.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class CollectionInstrumentMessageDTO {
    private String action;
    private UUID exerciseId;
    private UUID instrumentId;

    @JsonCreator
    public CollectionInstrumentMessageDTO(
            @JsonProperty("action") String action,
            @JsonProperty("exercise_id") String exerciseId,
            @JsonProperty("instrument_id") String instrumentId){
        this.action = action;
        this.exerciseId = UUID.fromString(exerciseId);
        this.instrumentId = UUID.fromString(instrumentId);
    }
}
