package uk.gov.ons.ctp.response.collection.exercise.domain;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.cobertura.CoverageIgnore;

@CoverageIgnore
@Data
@Entity
@NoArgsConstructor
@Table(name = "samplelink", schema = "collectionexercise")
public class SampleLink {
  
  @Column(name = "collectionexerciseid")
  private UUID collectionExerciseId;
  
  @Column(name = "samplesummaryid")
  private UUID sampleSummaryId;

}
