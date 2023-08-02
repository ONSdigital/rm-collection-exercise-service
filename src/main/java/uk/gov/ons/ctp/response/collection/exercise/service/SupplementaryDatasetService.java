package uk.gov.ons.ctp.response.collection.exercise.service;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.collection.exercise.domain.SupplementaryDatasetEntity;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.SupplementaryDatasetDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.SupplementaryDatasetRepository;

@Service
public class SupplementaryDatasetService {

  @Autowired private SupplementaryDatasetRepository supplementaryDatasetRepository;

  public SupplementaryDatasetEntity addSupplementaryDatasetEntity(
      SupplementaryDatasetDTO supplementaryDatasetDTO) {

    SupplementaryDatasetEntity supplementaryDatasetEntity = new SupplementaryDatasetEntity();

    supplementaryDatasetEntity.setSupplementaryDatasetId(supplementaryDatasetDTO.getDatasetId());
    supplementaryDatasetEntity.setFormTypes(
        supplementaryDatasetDTO
            .getFormTypes()
            .stream()
            .distinct()
            .collect(Collectors.toMap(s -> s, s -> s)));

    supplementaryDatasetRepository.save(supplementaryDatasetEntity);

    return supplementaryDatasetEntity;
  }
}
