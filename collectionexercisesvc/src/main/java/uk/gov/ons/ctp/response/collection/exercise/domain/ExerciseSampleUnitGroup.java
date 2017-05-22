package uk.gov.ons.ctp.response.collection.exercise.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Domain model object for sample unit groups.
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "sampleunitgroup", schema = "collectionexercise")
public class ExerciseSampleUnitGroup {

  @Id
  @GenericGenerator(name = "sampleunitgroupseq_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
      @Parameter(name = "sequence_name", value = "collectionexercise.sampleunitgroupidseq"),
      @Parameter(name = "increment_size", value = "1")
  })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sampleunitgroupseq_gen")
  @Column(name = "sampleunitgroupid")
  private Integer sampleUnitGroupId;

  @ManyToOne
  @JoinColumn(name = "exercisePK", referencedColumnName = "exercisePK")
  private CollectionExercise collectionExercise;

  @Column(name = "formtype")
  private String formType;

  @Enumerated(EnumType.STRING)
  private SampleUnitGroupState state;

  @Column(name = "createddatetime")
  private Timestamp createdDateTime;

  @Column(name = "modifieddatetime")
  private Timestamp modifiedDateTime;

}
