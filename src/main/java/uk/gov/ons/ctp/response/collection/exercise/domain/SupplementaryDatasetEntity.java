package uk.gov.ons.ctp.response.collection.exercise.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

/** Domain model object. */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "supplementarydataset", schema = "supplementarydataset")
public class SupplementaryDatasetEntity {

  @Id
  @GenericGenerator(
      name = "eventseq_gen",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
          @Parameter(name = "sequence_name", value = "collectionexercise.supplementarydatasetidseq"),
          @Parameter(name = "increment_size", value = "1")
      })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "eventseq_gen")
  @Column(name = "id")
  private Integer id;

  @Column(name = "exercise_FK")
  private Integer exerciseFK;

  @Column(name = "supplementary_dataset_id")
  UUID supplementaryDatasetId;

  @Type(type = "jsonb")
  @Column(name = "attributes")
  List<String> formTypes;

}
