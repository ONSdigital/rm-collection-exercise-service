package uk.gov.ons.ctp.response.collection.exercise.domain;

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
  @Column(name = "sampleunitid")
  private Integer sampleUnitId;

  @ManyToOne
  @JoinColumn(name = "sampleunitgroupid", referencedColumnName = "sampleunitgroupid")
  private ExerciseSampleUnitGroup sampleUnitGroup;

  @Column(name = "sampleunitref")
  private String sampleUnitRef;

  @Column(name = "sampleunittype")
  private String sampleUnitType;

}
