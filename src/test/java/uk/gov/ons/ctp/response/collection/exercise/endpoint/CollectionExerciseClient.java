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

/**
 * A class to wrap the collection exercise REST API in Java using Unirest
 */
public class CollectionExerciseClient {

    private ObjectMapper jacksonMapper;
    private int port;
    private String username;
    private String password;

    /**
     * Constructor for client - accepts API connection information
     * @param aPort collection exercise API port
     * @param aUsername  collection exercise API username
     * @param aPassword collection exercise API password
     * @param aMapper an object mapper
     */
    public CollectionExerciseClient(final int aPort, final String aUsername, final String aPassword,
                                    final ObjectMapper aMapper) {
        this.port = aPort;
        this.jacksonMapper = aMapper;
        this.username = aUsername;
        this.password = aPassword;

        initialiseUnirestObjectMapper();
    }

    /**
     * Initialises object mapper as used by unirest (needs a Jackson ObjectMapper to construct)
     */
    private void initialiseUnirestObjectMapper() {
        Unirest.setObjectMapper(new com.mashape.unirest.http.ObjectMapper() {
            public <T> T readValue(final String value, final Class<T> valueType) {
                try {
                    return jacksonMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(final Object value) {
                try {
                    return jacksonMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Calls the API to create a collection exercise
     * @param surveyId survey to create collection exercise for
     * @param exerciseRef the period of the collection exercise
     * @param userDescription the description of the collection exercise
     * @return a Pair of the status code of the operation and the location at which the new resource has been created
     * @throws CTPException thrown if an error occurred creating the collection exercise
     */
    public Pair<Integer, String> createCollectionExercise(final UUID surveyId, final String exerciseRef,
                                                          final String userDescription)
            throws CTPException {
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

    /**
     * Calls the API to get a collection exercise from a UUID
     * @param collexId the uuid of the collection exercise
     * @return the full details of the collection exercise
     * @throws CTPException thrown if an error occurred retrieving the collection exercise details
     */
    public CollectionExerciseDTO getCollectionExercise(final UUID collexId) throws CTPException {
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

    /**
     * Calls the API to link a list of sample summaries to a collection exercise
     * @param collexId the uuid of the collection exercise to link
     * @param sampleSummaryIds a list of the uuids of the sample summaries to link
     * @return the http status code of the operation
     * @throws CTPException thrown if there was an error linking the sample summaries to the collection exercise
     */
    public int linkSampleSummaries(final UUID collexId, final List<UUID> sampleSummaryIds) throws CTPException {
        try {
            JSONObject jsonPayload = new JSONObject();
            JSONArray jsonSampleSummaryIds = new JSONArray();
            sampleSummaryIds.stream().forEach(ssi -> jsonSampleSummaryIds.put(ssi.toString()));
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

    /**
     * Links a sample summary to a collection exercise
     * @param collexId the uuid of the collection exercise to link
     * @param sampleSummaryId the uuid of the sample summary to link
     * @return the http status code of the operation
     * @throws CTPException thrown if there was an error linking the sample summary to the collection exercise
     * @see CollectionExerciseClient#linkSampleSummaries
     */
    public int linkSampleSummary(final UUID collexId, final UUID sampleSummaryId) throws CTPException {
        return linkSampleSummaries(collexId, Arrays.asList(sampleSummaryId));
    }

    /**
     * Gets a collection exercise given the whole URI (e.g. as returned in a Location header)
     * @param uriStr the full URI of the collection exercise resource
     * @return a representation of the collection exercise
     * @throws CTPException thrown if there was an error retrieving the collection exercise
     */
    public CollectionExerciseDTO getCollectionExercise(final String uriStr) throws CTPException {
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

    /**
     * Get the samples linked to a collection exercise
     * @param collexId the uuid of the collection exercise
     * @return a list of samole links
     * @throws CTPException thrown if an error occurs retrieving the sample links
     */
    public List<SampleLinkDTO> getSampleLinks(final UUID collexId) throws CTPException {
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
}
