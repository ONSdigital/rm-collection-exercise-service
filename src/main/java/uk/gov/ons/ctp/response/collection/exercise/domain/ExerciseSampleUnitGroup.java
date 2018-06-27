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
  @Column(name = "sampleunitgrouppk")
  private Integer sampleUnitGroupPK;

  @ManyToOne
  @JoinColumn(name = "exercisefk", referencedColumnName = "exercisepk")
  private CollectionExercise collectionExercise;

  @Column(name = "formtype")
  private String formType;

  @Enumerated(EnumType.STRING)
  private SampleUnitGroupState stateFK;

  @Column(name = "createddatetime")
  private Timestamp createdDateTime;

  @Column(name = "modifieddatetime")
  private Timestamp modifiedDateTime;
}
