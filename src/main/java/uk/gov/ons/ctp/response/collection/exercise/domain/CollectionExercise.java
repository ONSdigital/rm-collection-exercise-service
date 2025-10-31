package uk.gov.ons.ctp.response.collection.exercise.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;
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
  @Column(name = "exercise_pk")
  private Integer exercisePK;

  @Column(name = "actual_execution_date_time")
  private Timestamp actualExecutionDateTime;

  @Column(name = "scheduled_execution_date_time")
  private Timestamp scheduledExecutionDateTime;

  @Column(name = "scheduled_start_date_time")
  private Timestamp scheduledStartDateTime;

  @Column(name = "actual_publish_date_time")
  private Timestamp actualPublishDateTime;

  @Column(name = "period_start_date_time")
  private Timestamp periodStartDateTime;

  @Column(name = "period_end_date_time")
  private Timestamp periodEndDateTime;

  @Column(name = "scheduled_return_date_time")
  private Timestamp scheduledReturnDateTime;

  @Column(name = "scheduled_end_date_time")
  private Timestamp scheduledEndDateTime;

  @Column(name = "executed_by")
  private String executedBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "state_fk")
  private CollectionExerciseDTO.CollectionExerciseState state;

  @Column(name = "sample_size")
  private Integer sampleSize;

  @Column(name = "exercise_ref")
  private String exerciseRef;

  @Column(name = "user_description", length = 50)
  private String userDescription;

  @Column(name = "created")
  private Timestamp created;

  @Column(name = "updated")
  private Timestamp updated;

  @Column(name = "deleted")
  private Boolean deleted;

  @Column(name = "survey_id")
  private UUID surveyId;

  @OneToOne(mappedBy = "collectionExercise")
  @JsonManagedReference
  private SupplementaryDatasetEntity supplementaryDatasetEntity;
}
