package uk.gov.ons.ctp.response.collection.exercise.message.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupplementaryDataServiceDTO {

  @JsonProperty("survey_id") // "survey_id": "012"
  String surveyId;

  @JsonProperty("period_id") // "period_id": "2013"
  String periodId;

  @JsonProperty("form_types") // "form_types": ["0017","0123","0001"]
  List<String> formTypes;

  @JsonProperty("title") // "title": "A dataset version 4 for survey 2012 period 2013"
  String title;

  @JsonProperty("sds_published_at") // "sds_published_at": "2023-07-17T14:46:36Z"
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  Date sdsPublishedAt;

  @JsonProperty("total_reporting_units") // "total_reporting_units": 2
  int totalReportingUnits;

  @JsonProperty("schema_version") // "schema_version": "v1.0.0"
  String schemaVersion;

  @JsonProperty("sds_dataset_version") // "sds_dataset_version": 4
  int sdsDatasetVersion;

  @JsonProperty("filename") // "filename": "373d9a77-2ee5-4c1f-a6dd-8d07b0ea9793.json"
  String filename;

  @JsonProperty("dataset_id") // "dataset_id": "b9a87999-fcc0-4085-979f-06390fb5dddd"
  UUID datasetId;
}
