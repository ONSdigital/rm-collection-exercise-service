package uk.gov.ons.ctp.response.collection.exercise.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

/**
 * Domain model object.
 */
@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "casetypedefault", schema = "collectionexercise")
public class CaseTypeDefault implements CaseType {

  @Id
  @Column(name = "casetypedefaultpk")
  Integer caseTypeDefaultPK;

  @JoinColumn(name = "surveyfk", referencedColumnName = "surveypk")
  @Column(name = "surveyfk")
  Integer surveyFK;

  @Column(name = "actionplanid")
  UUID actionPlanId;

  @Column(name = "sampleunittypefk")
  String sampleUnitTypeFK;

  @Override
  public String toString() {
    return "CaseTypeDefault{" +
            "sampleUnitTypeFK='" + sampleUnitTypeFK + '\'' +
            ", actionPlanId=" + actionPlanId +
            ", caseTypeDefaultPK=" + caseTypeDefaultPK +
            ", surveyFK=" + surveyFK +
            '}';
  }


}
