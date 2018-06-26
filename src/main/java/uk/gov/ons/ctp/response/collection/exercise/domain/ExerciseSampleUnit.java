package uk.gov.ons.ctp.response.collection.exercise.domain;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;

/** Domain model object for sample units. */
@CoverageIgnore
@Entity
@Data
@NoArgsConstructor
@Table(name = "sampleunit", schema = "collectionexercise")
public class ExerciseSampleUnit {

  @Id
  @GenericGenerator(
      name = "sampleunitseq_gen",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
        @Parameter(name = "sequence_name", value = "collectionexercise.sampleunitpkseq"),
        @Parameter(name = "increment_size", value = "1")
      })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sampleunitseq_gen")
  @Column(name = "sampleunitpk")
  private Integer sampleUnitPK;

  @ManyToOne
  @JoinColumn(name = "sampleunitgroupfk", referencedColumnName = "sampleunitgrouppk")
  private ExerciseSampleUnitGroup sampleUnitGroup;

  @Column(name = "collectioninstrumentid")
  private UUID collectionInstrumentId;

  @Column(name = "partyid")
  private UUID partyId;

  @Column(name = "sampleunitref")
  private String sampleUnitRef;

  @Enumerated(EnumType.STRING)
  @Column(name = "sampleunittypefk")
  private SampleUnitDTO.SampleUnitType sampleUnitType;
}
