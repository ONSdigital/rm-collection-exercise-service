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

    try {
      if (supplementaryDatasetRepository.existsByExerciseFK(collectionExercise.getExercisePK())) {
        log.info("A supplementary dataset with exerciseFk has been found.");
        supplementaryDatasetRepository.deleteByExerciseFK(collectionExercise.getExercisePK());
        log.info("Supplementary dataset has been removed.");
      }
      saveNewSupplementaryDataset(collectionExercise, supplementaryDatasetDTO);
    } catch (Exception e) {
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR, "Something went wrong adding dataset {}", e);
    }
  }

  private void saveNewSupplementaryDataset(
      CollectionExercise collectionExercise, SupplementaryDatasetDTO supplementaryDatasetDTO)
      throws CTPException {
    SupplementaryDatasetEntity supplementaryDatasetEntity = new SupplementaryDatasetEntity();
    supplementaryDatasetEntity.setExerciseFK(collectionExercise.getExercisePK());
    supplementaryDatasetEntity.setSupplementaryDatasetId(supplementaryDatasetDTO.getDatasetId());
    ObjectMapper mapper = new ObjectMapper();

    try {
      String supplementaryDatasetJson = mapper.writeValueAsString(supplementaryDatasetDTO);
      supplementaryDatasetEntity.setSupplementaryDatasetJson(supplementaryDatasetJson);
    } catch (JsonProcessingException e) {
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR, "Something went wrong adding dataset {}", e);
    }
    supplementaryDatasetRepository.save(supplementaryDatasetEntity);
    log.info("Successfully saved the supplementary dataset to the database");
  }
}
