package uk.gov.ons.ctp.response.collection.exercise.domain;

import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/** Domain model object. */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "event", schema = "collectionexercise")
public class Event {

  private UUID id;

  @Id
  @GenericGenerator(
      name = "eventseq_gen",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
        @Parameter(name = "sequence_name", value = "collectionexercise.eventpkseq"),
        @Parameter(name = "increment_size", value = "1")
      })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "eventseq_gen")
  @Column(name = "eventpk")
  private Integer eventPK;

  @Column(name = "tag", length = 20)
  private String tag;

  @Column(name = "timestamp")
  private Timestamp timestamp;

  @ManyToOne
  @JoinColumn(name = "collexfk", referencedColumnName = "exercisePK")
  private CollectionExercise collectionExercise;

  private Timestamp created;
  private Timestamp updated;

  private Boolean deleted = false;

  @Column(name = "message_sent")
  private Timestamp messageSent;
}
