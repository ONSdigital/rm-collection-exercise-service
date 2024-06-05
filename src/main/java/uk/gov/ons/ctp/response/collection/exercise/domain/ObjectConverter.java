package uk.gov.ons.ctp.response.collection.exercise.domain;

import java.util.ArrayList;
import java.util.List;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleLinkDTO;

public class ObjectConverter {

  private ObjectConverter() {
  };

  public static List<SampleLinkDTO> sampleLinkDTO(List<SampleLink> sampleLinks) {
    List<SampleLinkDTO> mappedSampleLink = new ArrayList<>();
    for (int i = 0; i <= sampleLinks.size(); i++) {
      SampleLink oldSampleLink = sampleLinks.get(i);
      SampleLinkDTO sampleLinkDTO = new SampleLinkDTO();

      sampleLinkDTO.setSampleSummaryId(oldSampleLink.getSampleSummaryId().toString());
      sampleLinkDTO.setCollectionExerciseId(oldSampleLink.getCollectionExerciseId().toString());

      mappedSampleLink.add(sampleLinkDTO);
    }
    return mappedSampleLink;
  }

  public static CollectionExerciseDTO collectionExerciseDTO(CollectionExercise collectionExercise) {
    CollectionExerciseDTO collectionExerciseDTO = new CollectionExerciseDTO();
    collectionExerciseDTO.setExerciseRef(collectionExercise.getExerciseRef());
    collectionExerciseDTO.setCreated(collectionExercise.getCreated());
    collectionExerciseDTO.setDeleted(collectionExercise.getDeleted());
    collectionExerciseDTO.setId(collectionExercise.getId());
    collectionExerciseDTO.setActualExecutionDateTime(
        collectionExercise.getActualExecutionDateTime());
    collectionExerciseDTO.setActualPublishDateTime(collectionExercise.getActualPublishDateTime());
    collectionExerciseDTO.setExecutedBy(collectionExercise.getExecutedBy());
    collectionExerciseDTO.setPeriodEndDateTime(collectionExercise.getPeriodEndDateTime());
    collectionExerciseDTO.setPeriodStartDateTime(collectionExercise.getPeriodStartDateTime());
    collectionExerciseDTO.setScheduledEndDateTime(collectionExercise.getScheduledEndDateTime());
    collectionExerciseDTO.setScheduledExecutionDateTime(
        collectionExercise.getScheduledExecutionDateTime());
    collectionExerciseDTO.setScheduledReturnDateTime(
        collectionExercise.getScheduledReturnDateTime());
    collectionExerciseDTO.setScheduledStartDateTime(collectionExercise.getScheduledStartDateTime());
    collectionExerciseDTO.setState(collectionExercise.getState());
    collectionExerciseDTO.setSurveyId(collectionExercise.getSurveyId().toString());
    collectionExerciseDTO.setUpdated(collectionExercise.getUpdated());
    collectionExerciseDTO.setUserDescription(collectionExercise.getUserDescription());
    collectionExerciseDTO.setSupplementaryDatasetEntity(
        collectionExercise.getSupplementaryDatasetEntity());
    collectionExerciseDTO.setSampleSize(collectionExercise.getSampleSize());

    return collectionExerciseDTO;
  }
}
