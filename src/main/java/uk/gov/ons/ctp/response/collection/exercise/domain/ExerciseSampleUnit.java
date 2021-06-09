package uk.gov.ons.ctp.response.collection.exercise.domain;

import java.util.Objects;
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
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitParentDTO;

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
  @Column(name = "sample_unit_pk")
  private Integer sampleUnitPK;

  @ManyToOne
  @JoinColumn(name = "sample_unit_group_fk", referencedColumnName = "sample_unit_group_pk")
  private ExerciseSampleUnitGroup sampleUnitGroup;

  @Column(name = "id")
  private UUID sampleUnitId;

  @Column(name = "collection_instrument_id")
  private UUID collectionInstrumentId;

  @Column(name = "party_id")
  private UUID partyId;

  @Column(name = "sample_unit_ref")
  private String sampleUnitRef;

  @Enumerated(EnumType.STRING)
  @Column(name = "sample_unit_type_fk")
  private SampleUnitDTO.SampleUnitType sampleUnitType;

  public SampleUnitParentDTO toSampleUnitParent(
      final boolean activeEnrolment, final UUID collectionExerciseId) {
    final SampleUnitParentDTO parent = new SampleUnitParentDTO();
    populateSampleUnit(activeEnrolment, parent);
    parent.setCollectionExerciseId(collectionExerciseId.toString());

    return parent;
  }

  private void populateSampleUnit(final boolean activeEnrolment, final SampleUnit sampleUnit) {
    sampleUnit.setActiveEnrolment(activeEnrolment);
    sampleUnit.setId(getSampleUnitId().toString());
    sampleUnit.setSampleUnitRef(getSampleUnitRef());
    sampleUnit.setSampleUnitType(getSampleUnitType().name());
    sampleUnit.setPartyId(Objects.toString(getPartyId(), null));
    sampleUnit.setCollectionInstrumentId(getCollectionInstrumentId().toString());
  }
}
