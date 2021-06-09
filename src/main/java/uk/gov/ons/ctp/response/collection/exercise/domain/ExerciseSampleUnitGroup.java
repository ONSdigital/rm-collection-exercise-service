package uk.gov.ons.ctp.response.collection.exercise.domain;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;

/** Domain model object for sample unit groups. */
@CoverageIgnore
@Entity
@Data
@NoArgsConstructor
@Table(name = "sampleunitgroup", schema = "collectionexercise")
public class ExerciseSampleUnitGroup {

  @Id
  @GenericGenerator(
      name = "sampleunitgroupseq_gen",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
        @Parameter(name = "sequence_name", value = "collectionexercise.sampleunitgrouppkseq"),
        @Parameter(name = "increment_size", value = "1")
      })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sampleunitgroupseq_gen")
  @Column(name = "sample_unit_group_pk")
  private Integer sampleUnitGroupPK;

  @ManyToOne
  @JoinColumn(name = "exercise_fk", referencedColumnName = "exercise_pk")
  private CollectionExercise collectionExercise;

  @Column(name = "form_type")
  private String formType;

  @Enumerated(EnumType.STRING)
  @Column(name = "state_fk")
  private SampleUnitGroupState stateFK;

  @Column(name = "created_date_time")
  private Timestamp createdDateTime;

  @Column(name = "modified_date_time")
  private Timestamp modifiedDateTime;
}
