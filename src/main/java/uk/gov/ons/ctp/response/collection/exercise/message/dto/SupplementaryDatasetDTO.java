package uk.gov.ons.ctp.response.collection.exercise.message.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupplementaryDatasetDTO {

  @JsonProperty("survey_id")
  String surveyId;

  @JsonProperty("period_id")
  String periodId;

  @JsonProperty("form_types")
  Map<String, String> formTypes;

  @JsonProperty("title")
  String title;

  @JsonProperty("sds_published_at")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  Date sdsPublishedAt;

  @JsonProperty("total_reporting_units")
  int totalReportingUnits;

  @JsonProperty("schema_version")
  String schemaVersion;

  @JsonProperty("sds_dataset_version")
  int sdsDatasetVersion;

  @JsonProperty("filename")
  String filename;

  @JsonProperty("dataset_id")
  UUID datasetId;
}
