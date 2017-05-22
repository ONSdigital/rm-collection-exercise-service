package uk.gov.ons.ctp.response.collection.exercise.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Domain model object for Survey.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "survey", schema = "collectionexercise")
public class Survey {

  @Id
  @Column(name = "surveyid")
  private String surveyId;

  @Column(name = "surveyref")
  private String surveyRef;

}
