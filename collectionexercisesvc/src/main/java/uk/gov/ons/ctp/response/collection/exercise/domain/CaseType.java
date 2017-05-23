package uk.gov.ons.ctp.response.collection.exercise.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Domain model object.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "casetypeoverride", schema = "collectionexercise")
public class CaseType {

  @Id
  @Column(name = "casetypeoverridepk")
  Integer caseTypeOverridePK;

  @JoinColumn(name = "exercisefk", referencedColumnName = "exercisepk")
  @Column(name = "exercisefk")
  Integer exerciseFK;

  @Column(name = "sampleunittypefk")
  String sampleUnitTypeFK;

  @Column(name = "actionplanid")
  String actionPlanId;

}
