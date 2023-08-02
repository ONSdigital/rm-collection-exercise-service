package uk.gov.ons.ctp.response.collection.exercise.service;

import java.util.Collections;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.collection.exercise.domain.SupplementaryDatasetEntity;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.SupplementaryDatasetDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.SupplementaryDatasetRepository;

@RunWith(MockitoJUnitRunner.class)
public class SupplementaryDatasetServiceTest {

  @InjectMocks private SupplementaryDatasetService supplementaryDatasetService;

  @Mock private SupplementaryDatasetRepository supplementaryDatasetRepository;

  @Test
  public void testDbObjectCreation() {

    SupplementaryDatasetDTO supplementaryDatasetDTO = new SupplementaryDatasetDTO();

    UUID datasetId = UUID.randomUUID();

    supplementaryDatasetDTO.setDatasetId(datasetId);

    supplementaryDatasetDTO.setFormTypes(
        Collections.singletonList(
            "[\n" + "       \"0017\",\n" + "       \"0123\",\n" + "       \"0001\"\n" + "      ]"));

    SupplementaryDatasetEntity supplementaryDatasetEntity =
        supplementaryDatasetService.addSupplementaryDatasetEntity(supplementaryDatasetDTO);
  }
}
