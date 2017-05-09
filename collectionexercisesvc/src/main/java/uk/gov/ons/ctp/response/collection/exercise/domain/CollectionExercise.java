package uk.gov.ons.ctp.response.collection.exercise.domain;

import java.math.BigInteger;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model object.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "collectionexercise", schema = "collectionexercise")
public class CollectionExercise {

  /**
   * enum for collection exercise state
   */
  public enum CollectionExerciseState {
    PENDING,
    EXECUTED,
    VALIDATED,
    PUBLISHED;
  }

  @Id
  @GenericGenerator(name = "exerciseseq_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
      @Parameter(name = "sequence_name", value = "collectionexercise.exerciseidseq"),
      @Parameter(name = "increment_size", value = "1")
  })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exerciseseq_gen")
  @Column(name = "exerciseid")
  private BigInteger exerciseId;

  @Column(name = "scheduledstartdatetime")
  private Timestamp scheduledStartDateTime;

  @Column(name = "scheduledexecutiondatetime")
  private Timestamp scheduledExecutionDateTime;

  @Column(name = "scheduledreturndatetime")
  private Timestamp scheduledreturnDateTime;

  @Column(name = "scheduledenddatetime")
  private Timestamp scheduledEndDateTime;

  @Column(name = "scheduledsurveydate")
  private Timestamp scheduledSurveyDate;

  @Column(name = "actualpublishdatetime")
  private Timestamp actualPublishDateTime;

  @Column(name = "executedby")
  private String executedBy;

  @Enumerated(EnumType.STRING)
  private CollectionExerciseState state;

}
