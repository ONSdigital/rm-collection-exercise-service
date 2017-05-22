package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SampleSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.sample.representation.CollectionExerciseJobCreationRequestDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;

/**
 * HTTP RestClient implementation for calls to the Sample service
 *
 */
@Component
public class SampleSvcRestClientImpl implements SampleSvcClient {

  @Autowired
  private AppConfig appConfig;

  @Autowired
  private RestClient sampleSvcClientRestTemplate;

  @Override
  public SampleUnitsRequestDTO requestSampleUnits(CollectionExercise exercise) {

    CollectionExerciseJobCreationRequestDTO requestDTO = new CollectionExerciseJobCreationRequestDTO();
    requestDTO.setCollectionExerciseId(exercise.getId().toString()); //TODO remove .toString() when SampleService updated to use UUIDs
    requestDTO.setSurveyRef(exercise.getSurvey().getSurveyRef());
    requestDTO.setExerciseDateTime(exercise.getScheduledStartDateTime());
    return sampleSvcClientRestTemplate.postResource(appConfig.getSampleSvc().getRequestSampleUnitsPath(), requestDTO,
        SampleUnitsRequestDTO.class);
  }

}
