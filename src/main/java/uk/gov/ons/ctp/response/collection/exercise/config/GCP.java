package uk.gov.ons.ctp.response.collection.exercise.config;

import lombok.Data;

/** Config POJO for GCP params */
@Data
public class GCP {
  String project;
  String sampleDistributionTopic;
}
