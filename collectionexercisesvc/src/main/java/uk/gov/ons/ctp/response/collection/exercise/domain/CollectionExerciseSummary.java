package uk.gov.ons.ctp.response.collection.exercise.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

/**
 * Domain model object.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionExerciseSummary {

  @Id
  private String id;

  private String name;

  private Timestamp scheduledExecution;

}
