package uk.gov.ons.ctp.response.collection.exercise.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SupplementaryDatasetEntity;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.SupplementaryDatasetDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.SupplementaryDatasetRepository;

@Service
public class SupplementaryDatasetService {

  @Autowired private CollectionExerciseService collectionExerciseService;
  @Autowired private SupplementaryDatasetRepository supplementaryDatasetRepository;

  private static final Logger log = LoggerFactory.getLogger(SupplementaryDatasetService.class);

  @Transactional
  public void addSupplementaryDatasetEntity(SupplementaryDatasetDTO supplementaryDatasetDTO)
      throws CTPException {

    CollectionExercise collectionExercise =
        collectionExerciseService.findCollectionExercise(
            supplementaryDatasetDTO.getSurveyId(), supplementaryDatasetDTO.getPeriodId());
    if (collectionExercise == null) {
      log.error(
              "Failed to find collection exercise for supplementary dataset. "
                      + "survey_id: {}, period_id: {}",
              supplementaryDatasetDTO.getSurveyId(),
              supplementaryDatasetDTO.getPeriodId());
      throw new CTPException(
              CTPException.Fault.RESOURCE_NOT_FOUND,
              "Failed to find collection exercise for supplementary dataset");
    }
    try {
      if (existsByExerciseFK(collectionExercise.getExercisePK())) {
        log.info(
            "Supplementary dataset with exerciseFk {} has been found.",
            collectionExercise.getExercisePK());
        supplementaryDatasetRepository.deleteByExerciseFK(collectionExercise.getExercisePK());
        log.info("Supplementary dataset has been removed.");
      }
      SupplementaryDatasetEntity supplementaryDatasetEntity =
          createSupplementaryDatasetEntity(collectionExercise, supplementaryDatasetDTO);
      supplementaryDatasetRepository.save(supplementaryDatasetEntity);
      log.info("Successfully saved the supplementary dataset to the database");
    } catch (Exception e) {
      throw new CTPException(
              CTPException.Fault.SYSTEM_ERROR, "Something went wrong adding dataset", e);
    }
  }

  private SupplementaryDatasetEntity createSupplementaryDatasetEntity(
      CollectionExercise collectionExercise, SupplementaryDatasetDTO supplementaryDatasetDTO)
      throws CTPException {
    SupplementaryDatasetEntity supplementaryDatasetEntity = new SupplementaryDatasetEntity();
    supplementaryDatasetEntity.setExerciseFK(collectionExercise.getExercisePK());
    supplementaryDatasetEntity.setSupplementaryDatasetId(supplementaryDatasetDTO.getDatasetId());
    ObjectMapper mapper = new ObjectMapper();

    try {
      String supplementaryDatasetJson = mapper.writeValueAsString(supplementaryDatasetDTO);
      supplementaryDatasetEntity.setSupplementaryDatasetJson(supplementaryDatasetJson);
      return supplementaryDatasetEntity;
    } catch (JsonProcessingException e) {
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR,
          "Something went wrong deserializing Supplementary Dataset",
          e);
    }
  }

  public SupplementaryDatasetEntity findSupplementaryDataset(int exercisePk) {
    return supplementaryDatasetRepository.findByExerciseFK(exercisePk);
  }

  public boolean existsByExerciseFK(int exercisePK) {
    return supplementaryDatasetRepository.existsByExerciseFK(exercisePK);
  }
}
