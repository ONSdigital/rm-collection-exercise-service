package uk.gov.ons.ctp.response.collection.exercise.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SupplementaryDatasetEntity;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.SupplementaryDatasetDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.SupplementaryDatasetRepository;

@RunWith(MockitoJUnitRunner.class)
public class SupplementaryDatasetServiceTest {

  @InjectMocks private SupplementaryDatasetService supplementaryDatasetService;

  @Mock private SupplementaryDatasetRepository supplementaryDatasetRepository;

  @Mock private CollectionExerciseService collectionExerciseService;

  CollectionExercise collectionExercise = createCollectionExercise();

  SupplementaryDatasetDTO supplementaryDatasetDTO = createSupplementaryDataSet();

  @Test
  public void testSaveSupplementaryDataset() throws CTPException, JsonProcessingException {
    when(collectionExerciseService.findCollectionExercise(
            supplementaryDatasetDTO.getSurveyId(), supplementaryDatasetDTO.getPeriodId()))
        .thenReturn(collectionExercise);

    SupplementaryDatasetEntity supplementaryDatasetEntity = createSupplementaryDatasetEntity();
    supplementaryDatasetService.addSupplementaryDatasetEntity(supplementaryDatasetDTO);

    verify(supplementaryDatasetRepository, times(1)).save(supplementaryDatasetEntity);
  }

  @Test
  public void testDeleteAndSaveSupplementaryDataset() throws CTPException, JsonProcessingException {
    when(collectionExerciseService.findCollectionExercise(
            supplementaryDatasetDTO.getSurveyId(), supplementaryDatasetDTO.getPeriodId()))
        .thenReturn(collectionExercise);
    when(supplementaryDatasetRepository.existsByExerciseFK(collectionExercise.getExercisePK()))
        .thenReturn(true);

    SupplementaryDatasetEntity supplementaryDatasetEntity = createSupplementaryDatasetEntity();

    supplementaryDatasetService.addSupplementaryDatasetEntity(supplementaryDatasetDTO);

    verify(supplementaryDatasetRepository, times(1))
        .deleteByExerciseFK(collectionExercise.getExercisePK());
    verify(supplementaryDatasetRepository, times(1)).save(supplementaryDatasetEntity);
  }

  @Test(expected = CTPException.class)
  public void testFailedToFindCollectionExerciseForSupplementaryDataset() throws CTPException {
    when(collectionExerciseService.findCollectionExercise(
            supplementaryDatasetDTO.getSurveyId(), supplementaryDatasetDTO.getPeriodId()))
        .thenReturn(null);

    supplementaryDatasetService.addSupplementaryDatasetEntity(supplementaryDatasetDTO);
  }

  @Test
  public void testFailedToFindCollectionExerciseForSupplementaryDatasetResourceNotFoundException() {
    when(collectionExerciseService.findCollectionExercise(
            supplementaryDatasetDTO.getSurveyId(), supplementaryDatasetDTO.getPeriodId()))
        .thenReturn(null);
    try {
      supplementaryDatasetService.addSupplementaryDatasetEntity(supplementaryDatasetDTO);

      fail("Expected CTPException was not thrown");
    } catch (CTPException e) {
      assertEquals(CTPException.class, e.getClass());
      assertEquals(CTPException.Fault.RESOURCE_NOT_FOUND, e.getFault());
    }
  }

  @Test
  public void testFindSupplementaryDataset() throws JsonProcessingException {
    createSupplementaryDatasetEntity();

    supplementaryDatasetService.findSupplementaryDataset(collectionExercise.getExercisePK());

    verify(supplementaryDatasetRepository, times(1))
        .findByExerciseFK(collectionExercise.getExercisePK());
  }

  private SupplementaryDatasetDTO createSupplementaryDataSet() {
    UUID datasetId = UUID.randomUUID();

    SupplementaryDatasetDTO supplementaryDatasetDTO = new SupplementaryDatasetDTO();

    supplementaryDatasetDTO.setDatasetId(datasetId);

    supplementaryDatasetDTO.setFormTypes(
        Collections.singletonList(
            "       \"0017\",\n" + "       \"0123\",\n" + "       \"0001\"\n"));

    supplementaryDatasetDTO.setPeriodId("2013");

    supplementaryDatasetDTO.setSurveyId("009");

    return supplementaryDatasetDTO;
  }

  private CollectionExercise createCollectionExercise() {
    UUID collectionExerciseId = UUID.randomUUID();

    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(collectionExerciseId);
    collectionExercise.setExercisePK(1);

    return collectionExercise;
  }

  private SupplementaryDatasetEntity createSupplementaryDatasetEntity()
      throws JsonProcessingException {
    SupplementaryDatasetEntity supplementaryDatasetEntity = new SupplementaryDatasetEntity();
    supplementaryDatasetEntity.setExerciseFK(collectionExercise.getExercisePK());
    supplementaryDatasetEntity.setSupplementaryDatasetId(supplementaryDatasetDTO.getDatasetId());
    ObjectMapper mapper = new ObjectMapper();
    String supplementaryDatasetJson = mapper.writeValueAsString(supplementaryDatasetDTO);
    supplementaryDatasetEntity.setSupplementaryDatasetJson(supplementaryDatasetJson);
    return supplementaryDatasetEntity;
  }
}
