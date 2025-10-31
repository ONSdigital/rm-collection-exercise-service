package uk.gov.ons.ctp.response.collection.exercise.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Parameter;
import org.hibernate.type.SqlTypes;

/** Domain model object. */
@CoverageIgnore
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "supplementarydataset", schema = "collectionexercise")
public class SupplementaryDatasetEntity {

  @Id
  @GenericGenerator(
      name = "supplementarydatasetid_seq",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
        @Parameter(name = "sequence_name", value = "collectionexercise.supplementarydatasetidseq"),
        @Parameter(name = "increment_size", value = "1")
      })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "supplementarydatasetid_seq")
  @Column(name = "id")
  private Integer id;

  @Column(name = "exercise_fk")
  private Integer exerciseFK;

  @Column(name = "supplementary_dataset_id")
  UUID supplementaryDatasetId;

  @Column(name = "attributes")
  @JdbcTypeCode(SqlTypes.JSON)
  private String supplementaryDatasetJson;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exercise_fk", insertable = false, updatable = false)
  @JsonBackReference
  private CollectionExercise collectionExercise;
}
