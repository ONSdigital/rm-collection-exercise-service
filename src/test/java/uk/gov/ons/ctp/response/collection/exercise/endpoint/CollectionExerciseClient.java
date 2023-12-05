package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import uk.gov.ons.ctp.lib.common.UnirestInitialiser;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.ResponseEventDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleLinkDTO;

/** A class to wrap the collection exercise REST API in Java using Unirest */
public class CollectionExerciseClient {

  private ObjectMapper jacksonMapper;
  private int port;
  private String username;
  private String password;

  /**
   * Constructor for client - accepts API connection information
   *
   * @param aPort collection exercise API port
   * @param aUsername collection exercise API username
   * @param aPassword collection exercise API password
   * @param aMapper an object mapper
   */
  public CollectionExerciseClient(
      final int aPort, final String aUsername, final String aPassword, final ObjectMapper aMapper) {
    this.port = aPort;
    this.jacksonMapper = aMapper;
    this.username = aUsername;
    this.password = aPassword;

    UnirestInitialiser.initialise(jacksonMapper);
  }

  public List<EventDTO> getEvents(final UUID collexId) throws CTPException {
    try {
      return new ArrayList<>(
          Arrays.asList(
              Unirest.get("http://localhost:" + this.port + "/collectionexercises/{id}/events")
                  .routeParam("id", collexId.toString())
                  .basicAuth(username, password)
                  .header("accept", "application/json")
                  .asObject(EventDTO[].class)
                  .getBody()));
    } catch (UnirestException e) {
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR,
          String.format("Failed to get events for collection exercise: %s", collexId),
          e);
    }
  }

  public HttpResponse updateEvent(final EventDTO event) {
    final OffsetDateTime offsetDateTime =
        OffsetDateTime.ofInstant(event.getTimestamp().toInstant(), ZoneOffset.systemDefault());
    final String date = DateTimeFormatter.ISO_DATE_TIME.format(offsetDateTime);

    HttpResponse response = null;
    try {
      response =
          Unirest.put("http://localhost:" + this.port + "/collectionexercises/{id}/events/{tag}")
              .routeParam("id", event.getCollectionExerciseId().toString())
              .routeParam("tag", event.getTag())
              .basicAuth(this.username, this.password)
              .header("accept", "application/json")
              .header("Content-Type", "text/plain")
              .body(date)
              .asObject(ResponseEventDTO.class);
    } catch (UnirestException e) {
      throw new RuntimeException(
          String.format(
              "Could not update collection exercise events colletionExerciseId=%s tag=%s",
              event.getCollectionExerciseId(), event.getTag()),
          e);
    }
    if (response.getStatus() != HttpStatus.OK.value() && response.getStatus() != 409) {
      throw new RuntimeException(
          String.format(
              "Could not update collection exercise events colletionExerciseId=%s tag=%s status=%s",
              event.getCollectionExerciseId(), event.getTag(), response.getStatus()));
    }
    return response;
  }

  /**
   * Calls the API to create a collection exercise
   *
   * @param surveyId survey to create collection exercise for
   * @param exerciseRef the period of the collection exercise
   * @param userDescription the description of the collection exercise
   * @return a Pair of the status code of the operation and the location at which the new resource
   *     has been created
   * @throws CTPException thrown if an error occurred creating the collection exercise
   */
  public Pair<Integer, String> createCollectionExercise(
      final UUID surveyId, final String exerciseRef, final String userDescription)
      throws CTPException {
    CollectionExerciseDTO inputDto = new CollectionExerciseDTO();

    inputDto.setSurveyId(surveyId.toString());
    inputDto.setExerciseRef(exerciseRef);
    inputDto.setUserDescription(userDescription);
    inputDto.setSupplementaryDatasetEntity(null);

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
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR, "Failed to create collection exercise: %s", e);
    }
  }

  /**
   * Calls the API to get a collection exercise from a UUID
   *
   * @param collexId the uuid of the collection exercise
   * @return the full details of the collection exercise
   * @throws CTPException thrown if an error occurred retrieving the collection exercise details
   */
  CollectionExerciseDTO getCollectionExercise(final UUID collexId) throws CTPException {
    try {
      return Unirest.get("http://localhost:" + this.port + "/collectionexercises/{id}")
          .routeParam("id", collexId.toString())
          .basicAuth(this.username, this.password)
          .header("accept", "application/json")
          .asObject(CollectionExerciseDTO.class)
          .getBody();
    } catch (UnirestException e) {
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR,
          String.format("Failed to get collection exercise: %s", collexId),
          e);
    }
  }

  /**
   * Gets a collection exercise given the whole URI (e.g. as returned in a Location header)
   *
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
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR, "Failed to get collection exercise: %s", e);
    }
  }

  /**
   * Calls the API to link a list of sample summaries to a collection exercise
   *
   * @param collexId the uuid of the collection exercise to link
   * @param sampleSummaryIds a list of the uuids of the sample summaries to link
   * @return the http status code of the operation
   * @throws CTPException thrown if there was an error linking the sample summaries to the
   *     collection exercise
   */
  private int linkSampleSummaries(final UUID collexId, final List<UUID> sampleSummaryIds)
      throws CTPException {
    try {
      JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
      for (UUID ssi : sampleSummaryIds) {
        arrayBuilder.add(ssi.toString());
      }
      JsonArray sampleSummaryArray = arrayBuilder.build();
      JsonObject jsonPayload =
          Json.createObjectBuilder().add("sampleSummaryIds", sampleSummaryArray).build();

      HttpResponse<JsonNode> linkResponse =
          Unirest.put("http://localhost:" + this.port + "/collectionexercises/link/{id}")
              .routeParam("id", collexId.toString())
              .basicAuth(this.username, this.password)
              .header("accept", "application/json")
              .header("Content-Type", "application/json")
              .body(jsonPayload)
              .asJson();

      return linkResponse.getStatus();
    } catch (UnirestException e) {
      throw new CTPException(CTPException.Fault.SYSTEM_ERROR, "Failed to serialize payload: %s", e);
    }
  }

  /**
   * Links a sample summary to a collection exercise
   *
   * @param collexId the uuid of the collection exercise to link
   * @param sampleSummaryId the uuid of the sample summary to link
   * @throws CTPException thrown if there was an error linking the sample summary to the collection
   *     exercise
   * @see CollectionExerciseClient#linkSampleSummaries
   */
  void linkSampleSummary(final UUID collexId, final UUID sampleSummaryId) throws CTPException {
    linkSampleSummaries(collexId, Collections.singletonList(sampleSummaryId));
  }

  /**
   * Get the samples linked to a collection exercise
   *
   * @param collexId the uuid of the collection exercise
   * @return a list of samole links
   * @throws CTPException thrown if an error occurs retrieving the sample links
   */
  List<SampleLinkDTO> getSampleLinks(final UUID collexId) throws CTPException {
    try {
      SampleLinkDTO[] linkArray =
          Unirest.get("http://localhost:" + this.port + "/collectionexercises/link/{id}")
              .routeParam("id", collexId.toString())
              .basicAuth(this.username, this.password)
              .header("accept", "application/vnd.ons.sdc.samplelink.v1+json")
              .asObject(SampleLinkDTO[].class)
              .getBody();

      return Arrays.asList(linkArray);
    } catch (UnirestException e) {
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR, "Failed to get collection exercise: %s", e);
    }
  }

  public void createCollectionExerciseEvent(EventDTO event) {
    HttpResponse<String> response = null;
    try {
      response =
          Unirest.post("http://localhost:" + this.port + "/collectionexercises/{id}/events")
              .routeParam("id", event.getCollectionExerciseId().toString())
              .basicAuth(this.username, this.password)
              .header("accept", "application/json")
              .header("Content-Type", "application/json")
              .body(event)
              .asString();
    } catch (UnirestException e) {
      throw new RuntimeException(
          String.format(
              "Could not create collection exercise events colletionExerciseId=%s tag=%s",
              event.getCollectionExerciseId(), event.getTag()),
          e);
    }
    if (response.getStatus() != 201 && response.getStatus() != 409) {
      throw new RuntimeException(
          String.format(
              "Could not create collection exercise events colletionExerciseId=%s tag=%s status=%s",
              event.getCollectionExerciseId(), event.getTag(), response.getStatus()));
    }
  }

  void sampleSummaryReadiness(final UUID sampleSummaryId) throws CTPException {
    sampleSummaryReady(sampleSummaryId);
  }

  private void sampleSummaryReady(final UUID sampleSummaryId) throws CTPException {
    try {
      JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
      arrayBuilder.add(sampleSummaryId.toString());
      JsonArray sampleSummaryArray = arrayBuilder.build();
      JsonObject jsonPayload =
          Json.createObjectBuilder().add("sampleSummaryIds", sampleSummaryArray).build();

      Unirest.put("http://localhost:" + this.port + "/sample/summary-readiness")
          .basicAuth(this.username, this.password)
          .header("accept", "application/json")
          .header("Content-Type", "application/json")
          .body(jsonPayload)
          .asJson();

    } catch (UnirestException e) {
      throw new CTPException(CTPException.Fault.SYSTEM_ERROR, "Failed to serialize payload: %s", e);
    }
  }
}
