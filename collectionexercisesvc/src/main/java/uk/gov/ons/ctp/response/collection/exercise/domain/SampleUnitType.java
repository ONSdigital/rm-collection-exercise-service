package uk.gov.ons.ctp.response.collection.exercise.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
@Table(name = "sampleunittype", schema = "collectionexercise")
public class SampleUnitType {

  @Id
  @Column(name = "sampleunittype")
  private String sampleUnitType;

}
