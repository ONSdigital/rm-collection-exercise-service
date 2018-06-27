package uk.gov.ons.ctp.response.collection.exercise.domain;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/** Domain model object. */
@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "casetypedefault", schema = "collectionexercise")
public class CaseTypeDefault implements CaseType {

  @Id
  @GenericGenerator(
      name = "casetypedefaultseq_gen",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
        @Parameter(name = "sequence_name", value = "collectionexercise.casetypedefaultseq"),
        @Parameter(name = "increment_size", value = "1")
      })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "casetypedefaultseq_gen")
  @Column(name = "casetypedefaultpk")
  private Integer caseTypeDefaultPK;

  @Column(name = "survey_uuid")
  private UUID surveyId;

  @Column(name = "actionplanid")
  private UUID actionPlanId;

  @Column(name = "sampleunittypefk")
  private String sampleUnitTypeFK;
}
