package uk.gov.ons.ctp.response.collection.exercise.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Domain model object.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "collectionexercise", schema = "collectionexercise")
public class CollectionExercise {

  @Column(name = "id")
  private UUID id;

  @Id
  @GenericGenerator(name = "exerciseseq_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
      @Parameter(name = "sequence_name", value = "collectionexercise.exerciseidseq"),
      @Parameter(name = "increment_size", value = "1")
  })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exerciseseq_gen")
  @Column(name = "exercisepk")
  private Integer exercisePK;

  @ManyToOne
  @JoinColumn(name = "surveyfk", referencedColumnName = "surveypk")
  private Survey survey;

  @Column(name = "scheduledstartdatetime")
  private Timestamp scheduledStartDateTime;

  @Column(name = "scheduledexecutiondatetime")
  private Timestamp scheduledExecutionDateTime;

  @Column(name = "scheduledreturndatetime")
  private Timestamp scheduledreturnDateTime;

  @Column(name = "scheduledenddatetime")
  private Timestamp scheduledEndDateTime;

/*  @Column(name = "scheduledsurveydate")
  private Timestamp scheduledSurveyDate;*/

  @Column(name = "actualexecutiondatetime")
  private Timestamp actualExecutionDateTime;

  @Column(name = "actualpublishdatetime")
  private Timestamp actualPublishDateTime;

  @Column(name = "executedby")
  private String executedBy;

  @Enumerated(EnumType.STRING)
  private CollectionExerciseDTO.CollectionExerciseState state;

  @Column(name = "samplesize")
  private Integer sampleSize;

}
