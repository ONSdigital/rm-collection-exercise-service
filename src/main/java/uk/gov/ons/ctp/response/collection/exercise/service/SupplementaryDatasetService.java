package uk.gov.ons.ctp.response.collection.exercise.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
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
  public SupplementaryDatasetEntity addSupplementaryDatasetEntity(
      SupplementaryDatasetDTO supplementaryDatasetDTO)
      throws CTPException, JsonProcessingException {

    CollectionExercise collectionExercise =
        collectionExerciseService.findCollectionExercise(
            supplementaryDatasetDTO.getSurveyId(), supplementaryDatasetDTO.getPeriodId());

    if (collectionExercise == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format(
              "Cannot find collection exercise for surveyRef={} and period={}",
              supplementaryDatasetDTO.getSurveyId(),
              supplementaryDatasetDTO.getPeriodId()));
    }

    int collectionExercisePk = collectionExercise.getExercisePK();

    if (supplementaryDatasetRepository.existsByExerciseFK(collectionExercisePk)) {
      log.info("A supplementary dataset with exerciseFk has been found.");
      log.info("Deleting supplementary dataset.");

      supplementaryDatasetRepository.deleteByExerciseFK(collectionExercisePk);

      log.info("Supplementary dataset has been removed.");
    }

    SupplementaryDatasetEntity supplementaryDatasetEntity = new SupplementaryDatasetEntity();

    supplementaryDatasetEntity.setExerciseFK(collectionExercisePk);
    supplementaryDatasetEntity.setSupplementaryDatasetId(supplementaryDatasetDTO.getDatasetId());
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<HashMap<String, String>> typeRef =
        new TypeReference<HashMap<String, String>>() {};
    Map<String, String> formTypes =
        mapper.readValue(supplementaryDatasetDTO.getFormTypes().toString(), typeRef);

    supplementaryDatasetEntity.setFormTypes(formTypes);
    //    supplementaryDatasetEntity.setFormTypes(
    //        supplementaryDatasetDTO
    //            .getFormTypes()
    //            .stream()
    //            .distinct()
    //            .collect(Collectors.toMap(s -> s, s -> s)));

    supplementaryDatasetRepository.save(supplementaryDatasetEntity);

    log.info("Successfully saved the supplementary dataset to the database");

    return supplementaryDatasetEntity;
  }
}
