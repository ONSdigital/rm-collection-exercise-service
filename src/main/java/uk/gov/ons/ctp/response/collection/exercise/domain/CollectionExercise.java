package uk.gov.ons.ctp.response.collection.exercise.domain;

import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;

/** Domain model object. */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "collectionexercise", schema = "collectionexercise")
public class CollectionExercise {

  private UUID id;

  @Id
  @GenericGenerator(
      name = "exerciseseq_gen",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
        @Parameter(name = "sequence_name", value = "collectionexercise.exercisepkseq"),
        @Parameter(name = "increment_size", value = "1")
      })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exerciseseq_gen")
  @Column(name = "exercisepk")
  private Integer exercisePK;

  private String name;

  @Column(name = "actualexecutiondatetime")
  private Timestamp actualExecutionDateTime;

  @Column(name = "scheduledexecutiondatetime")
  private Timestamp scheduledExecutionDateTime;

  @Column(name = "scheduledstartdatetime")
  private Timestamp scheduledStartDateTime;

  @Column(name = "actualpublishdatetime")
  private Timestamp actualPublishDateTime;

  @Column(name = "periodstartdatetime")
  private Timestamp periodStartDateTime;

  @Column(name = "periodenddatetime")
  private Timestamp periodEndDateTime;

  @Column(name = "scheduledreturndatetime")
  private Timestamp scheduledReturnDateTime;

  @Column(name = "scheduledenddatetime")
  private Timestamp scheduledEndDateTime;

  @Column(name = "executedby")
  private String executedBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "statefk")
  private CollectionExerciseDTO.CollectionExerciseState state;

  @Column(name = "samplesize")
  private Integer sampleSize;

  @Column(name = "exerciseref")
  private String exerciseRef;

  @Column(name = "user_description", length = 50)
  private String userDescription;

  @Column(name = "created")
  private Timestamp created;

  @Column(name = "updated")
  private Timestamp updated;

  @Column(name = "deleted")
  private Boolean deleted;

  @Column(name = "survey_uuid")
  private UUID surveyId;
}
