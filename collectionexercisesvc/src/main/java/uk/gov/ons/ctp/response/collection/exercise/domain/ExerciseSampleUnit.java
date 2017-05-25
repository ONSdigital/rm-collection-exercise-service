package uk.gov.ons.ctp.response.collection.exercise.domain;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
