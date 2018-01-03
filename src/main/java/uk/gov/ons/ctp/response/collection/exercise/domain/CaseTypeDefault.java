package uk.gov.ons.ctp.response.collection.exercise.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
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
  private Integer caseTypeDefaultPK;

  @Column(name="survey_uuid")
  private UUID surveyId;

  @Column(name = "actionplanid")
  private UUID actionPlanId;

  @Column(name = "sampleunittypefk")
  private String sampleUnitTypeFK;

}
