package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleLinkDTO;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CollectionExerciseClient {

    public CollectionExerciseClient(int port, String username, String password, ObjectMapper mapper){
        this.port = port;
        this.jacksonMapper = mapper;
        this.username = username;
        this.password = password;

        initialiseUnirestObjectMapper();
    }

    private ObjectMapper jacksonMapper;

    private void initialiseUnirestObjectMapper(){
        Unirest.setObjectMapper(new com.mashape.unirest.http.ObjectMapper() {
            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public Pair<Integer, String> createCollectionExercise(UUID surveyId, String exerciseRef, String userDescription) throws CTPException {
        CollectionExerciseDTO inputDto = new CollectionExerciseDTO();

        inputDto.setSurveyId(surveyId.toString());
        inputDto.setExerciseRef(exerciseRef);
        inputDto.setUserDescription(userDescription);

        try {
            HttpResponse<String> createCollexResponse =
                    Unirest.post("http://localhost:" + this.port + "/collectionexercises")
                            .basicAuth(this.username, this.password)
                            .header("accept", "application/json")
                            .header("Content-Type", "application/json")
                            .body(inputDto)
                            .asString();
            int statusCode = createCollexResponse.getStatus();
            List<String> locations = createCollexResponse.getHeaders().get("Location");
            String location = locations != null && locations.size() > 0 ? locations.get(0) : null;

            return new ImmutablePair<>(statusCode, location);
        } catch (UnirestException e) {
            throw new CTPException(CTPException.Fault.SYSTEM_ERROR, "Failed to create collection exercise", e);
        }
    }

    public CollectionExerciseDTO getCollectionExercise(UUID collexId) throws CTPException {
        try {
            return Unirest.get("http://localhost:" + this.port + "/collectionexercises/{id}")
                    .routeParam("id", collexId.toString())
                    .basicAuth(this.username, this.password)
                    .header("accept", "application/json")
                    .asObject(CollectionExerciseDTO.class)
                    .getBody();
        } catch (UnirestException e) {
            throw new CTPException(CTPException.Fault.SYSTEM_ERROR, "Failed to get collection exercise", e);
        }
    }

    public int linkSampleSummaries(UUID collexId, List<UUID> sampleSummaryIds) throws CTPException {
        try {
            JSONObject jsonPayload = new JSONObject();
            JSONArray jsonSampleSummaryIds = new JSONArray();
            sampleSummaryIds.stream().forEach( ssi -> jsonSampleSummaryIds.put(ssi.toString()) );
            jsonPayload.put("sampleSummaryIds", jsonSampleSummaryIds);

            HttpResponse<JsonNode> linkResponse =
                    Unirest.put("http://localhost:" + this.port + "/collectionexercises/link/{id}")
                            .routeParam("id", collexId.toString())
                            .basicAuth(this.username, this.password)
                            .header("accept", "application/json")
                            .header("Content-Type", "application/json")
                            .body(jsonPayload)
                            .asJson();

            return linkResponse.getStatus();
        } catch (JSONException e) {
            throw new CTPException(CTPException.Fault.SYSTEM_ERROR, "Failed to create payload", e);
        } catch (UnirestException e) {
            throw new CTPException(CTPException.Fault.SYSTEM_ERROR, "Failed to serialize payload", e);
        }
    }

    public int linkSampleSummary(UUID collexId, UUID sampleSummaryId) throws CTPException {
        return linkSampleSummaries(collexId, Arrays.asList(sampleSummaryId));
    }

    public CollectionExerciseDTO getCollectionExercise(String uriStr) throws CTPException {
        try {
            return Unirest.get(uriStr)
                    .basicAuth(this.username, this.password)
                    .header("accept", "application/json")
                    .asObject(CollectionExerciseDTO.class)
                    .getBody();
        } catch (UnirestException e) {
            throw new CTPException(CTPException.Fault.SYSTEM_ERROR, "Failed to get collection exercise", e);
        }
    }

    public List<SampleLinkDTO> getSampleLinks(UUID collexId) throws CTPException {
        try {
            SampleLinkDTO[] linkArray = Unirest.get("http://localhost:" + this.port + "/collectionexercises/link/{id}")
                        .routeParam("id", collexId.toString())
                        .basicAuth(this.username, this.password)
                        .header("accept", "application/vnd.ons.sdc.samplelink.v1+json")
                        .asObject(SampleLinkDTO[].class)
                        .getBody();

            return Arrays.asList(linkArray);
        } catch (UnirestException e) {
            throw new CTPException(CTPException.Fault.SYSTEM_ERROR, "Failed to get collection exercise", e);
        }
    }

    private int port;
    private String username;
    private String password;
}
