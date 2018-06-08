package uk.gov.ons.ctp.response.collection.exercise.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

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
  @GenericGenerator(name = "casetypeoverrideseq_gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
          parameters = {
                  @Parameter(name = "sequence_name", value = "collectionexercise.casetypeoverrideseq"),
                  @Parameter(name = "increment_size", value = "1")
          })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "casetypeoverrideseq_gen")
  @Column(name = "casetypeoverridepk")
  private Integer caseTypeOverridePK;

  @JoinColumn(name = "exercisefk", referencedColumnName = "exercisepk")
  @Column(name = "exercisefk")
  private Integer exerciseFK;

  @Column(name = "sampleunittypefk")
  private String sampleUnitTypeFK;

  @Column(name = "actionplanid")
  private UUID actionPlanId;

  @Override
  public String toString() {
    return "CaseTypeOverride{"
            + "sampleUnitTypeFK='" + sampleUnitTypeFK + '\''
            + ", actionPlanId=" + actionPlanId
            + ", caseTypeOverridePK=" + caseTypeOverridePK
            + ", exerciseFK=" + exerciseFK
            + '}';
  }
}
