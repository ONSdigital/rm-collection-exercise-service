package uk.gov.ons.ctp.response.collection.exercise.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.collection.exercise.domain.SupplementaryDatasetEntity;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.SupplementaryDatasetDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.SupplementaryDatasetRepository;

@Service
public class SupplementaryDatasetService {

  @Autowired
  private SupplementaryDatasetRepository supplementaryDatasetRepository;

  private void addSupplementaryDatasetEntity(SupplementaryDatasetDTO supplementaryDatasetDTO) {

    SupplementaryDatasetEntity supplementaryDatasetEntity = new SupplementaryDatasetEntity();

    supplementaryDatasetEntity.setSupplementaryDatasetId(supplementaryDatasetDTO.getDatasetId());
    supplementaryDatasetEntity.setFormTypes(supplementaryDatasetDTO.getFormTypes());

    supplementaryDatasetRepository.save(supplementaryDatasetEntity);

  }

}
