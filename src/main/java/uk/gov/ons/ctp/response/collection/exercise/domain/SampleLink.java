package uk.gov.ons.ctp.response.collection.exercise.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@CoverageIgnore
@Data
@Entity
@NoArgsConstructor
@Table(name = "samplelink", schema = "collectionexercise")
public class SampleLink {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "samplelinkseq_gen")
  @GenericGenerator(
      name = "samplelinkseq_gen",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
        @Parameter(name = "sequence_name", value = "collectionexercise.samplelinkpkseq"),
        @Parameter(name = "increment_size", value = "1")
      })
  @Column(name = "sample_link_pk")
  private Integer sampleLinkPK;

  @Column(name = "collection_exercise_id")
  private UUID collectionExerciseId;

  @Column(name = "sample_summary_id")
  private UUID sampleSummaryId;
}
