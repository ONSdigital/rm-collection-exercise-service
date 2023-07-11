package uk.gov.ons.ctp.response.collection.exercise.message.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupplementaryDataServiceDTO {

  String datasetId;
  String surveyId;
  String periodId;
  String title;
  String sdsSchemaVersion;
  int totalReportingUnits;
  String schemaVersion;
  String filename;
  List<String> formTypes;

}
