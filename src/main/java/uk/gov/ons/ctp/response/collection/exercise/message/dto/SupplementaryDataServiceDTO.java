package uk.gov.ons.ctp.response.collection.exercise.message.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupplementaryDataServiceDTO {

  /*  {
      "survey_id": "012",
      "period_id": "2013",
      "form_types": [
         "0017",
         "0123",
         "0001"
        ],
      "title": "A dataset version 4 for survey 2012 period 2013",
      "sds_published_at": "2023-07-17T14:46:36Z",
      "total_reporting_units": 2,
      "schema_version": "v1.0.0",
      "sds_dataset_version": 4,
      "filename": "373d9a77-2ee5-4c1f-a6dd-8d07b0ea9793.json",
      "dataset_id": "b9a87999-fcc0-4085-979f-06390fb5dddd"
  }*/

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
