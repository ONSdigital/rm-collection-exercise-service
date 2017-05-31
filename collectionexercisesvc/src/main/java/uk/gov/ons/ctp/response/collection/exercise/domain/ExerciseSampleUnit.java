package uk.gov.ons.ctp.response.collection.exercise.domain;

import java.util.UUID;

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

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model object for sample units.
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "sampleunit", schema = "collectionexercise")
public class ExerciseSampleUnit {

  @Id
  @GenericGenerator(name = "sampleunitseq_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
      @Parameter(name = "sequence_name", value = "collectionexercise.sampleunitpkseq"),
      @Parameter(name = "increment_size", value = "1")
  })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sampleunitseq_gen")
  @Column(name = "sampleunitpk")
  private Integer sampleUnitPK;

  @ManyToOne
  @JoinColumn(name = "sampleunitgroupfk", referencedColumnName = "sampleunitgrouppk")
  private ExerciseSampleUnitGroup sampleUnitGroup;

  @Column (name = "collectioninstrumentid")
  private UUID collectionInstrumentId;

  @Column (name = "partyid")
  private UUID partyId;

  @Column(name = "sampleunitref")
  private String sampleUnitRef;

  @Column(name = "sampleunittypefk")
  private String sampleUnitTypeFK;

}
