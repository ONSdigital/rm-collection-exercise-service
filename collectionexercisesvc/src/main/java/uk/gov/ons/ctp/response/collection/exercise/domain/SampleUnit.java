package uk.gov.ons.ctp.response.collection.exercise.domain;

import java.math.BigInteger;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
@Table(name = "exercisesampleunit", schema = "collectionexercise")
public class SampleUnit {

  @Id
  @Column(name = "sampleunitid")
  private BigInteger sampleUnitId;

  @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "sampleunitgroupid", referencedColumnName = "sampleunitgroupid")
  private SampleUnitGroup sampleUnitGroup;

  @Column(name = "sampleunitref")
  private String sampleUnitRef;

  @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "sampleunittype", referencedColumnName = "sampleunittype")
  private SampleUnitType sampleUnitType;

}
