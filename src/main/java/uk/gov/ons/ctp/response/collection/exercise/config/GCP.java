package uk.gov.ons.ctp.response.collection.exercise.config;

import lombok.Data;

/** Config POJO for GCP params */
@Data
public class GCP {
  String project;
  String caseNotificationTopic;
  String sampleSummaryActivationStatusSubscription;
  String sampleSummaryActivationTopic;
  String collectionExerciseEndTopic;
  String collectionExerciseEventStatusUpdateSubscription;
  String supplementaryDataServiceTopic;
  String supplementaryDataServiceSubscription;
}
