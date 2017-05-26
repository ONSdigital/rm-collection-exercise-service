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
@Table(name = "casetypeoverride", schema = "collectionexercise")
public class CaseTypeOverride implements CaseType {

  @Id
  @Column(name = "casetypeoverridepk")
  Integer caseTypeOverridePK;

  @JoinColumn(name = "exercisefk", referencedColumnName = "exercisepk")
  @Column(name = "exercisefk")
  Integer exerciseFK;

  @Column(name = "sampleunittypefk")
  String sampleUnitTypeFK;

  @Column(name = "actionplanid")
  UUID actionPlanId;

  @Override
  public String toString() {
    return "CaseTypeOverride{" +
            "sampleUnitTypeFK='" + sampleUnitTypeFK + '\'' +
            ", actionPlanId=" + actionPlanId +
            ", caseTypeOverridePK=" + caseTypeOverridePK +
            ", exerciseFK=" + exerciseFK +
            '}';
  }
}
