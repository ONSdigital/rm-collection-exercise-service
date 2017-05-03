package uk.gov.ons.ctp.response.collection.exercise.domain;

import java.math.BigInteger;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "exercisesampleunitgroup", schema = "collectionexercise")
public class SampleUnitGroup {

  @Id
  @GenericGenerator(name = "sampleunitgroupseq_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
      @Parameter(name = "sequence_name", value = "collectionexercise.sampleunitgroupidseq"),
      @Parameter(name = "increment_size", value = "1")
  })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sampleunitgroupseq_gen")
  @Column(name = "sampleunitgroupid")
  private BigInteger sampleUnitGroupId;

  @ManyToOne
  @JoinColumn(name = "exerciseid", referencedColumnName = "exerciseid")
  private CollectionExercise collectionExercise;

  @Column(name = "formtype")
  private String formType;

  private String state;

  @Column(name = "createddatetime")
  private Timestamp createdDateTime;

  @Column(name = "modifieddatetime")
  private Timestamp modifiedDateTime;

}
