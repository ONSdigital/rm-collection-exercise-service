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
@Table(name = "casetypedefault", schema = "collectionexercise")
public class CaseTypeDefault {

  @Id
  @Column(name = "casetypedefaultpk")
  Integer caseTypeDefaultPK;

  @JoinColumn(name = "surveyfk", referencedColumnName = "surveypk")
  @Column(name = "surveyfk")
  Integer surveyFK;

  @Column(name = "sampleunittypefk")
  String sampleUnitTypeFK;

  @Column(name = "actionplanid")
  String actionPlanId;

}
