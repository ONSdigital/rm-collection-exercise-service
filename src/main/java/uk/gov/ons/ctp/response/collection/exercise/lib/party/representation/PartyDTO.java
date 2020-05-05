package uk.gov.ons.ctp.response.collection.exercise.lib.party.representation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class PartyDTO {
  private String id;
  private String sampleUnitType;
  private String sampleSummaryId;
  private String sampleUnitRef;
  private String name;
  private Attributes attributes;
  private List<Association> associations;

  @JsonIgnore private String status; // required to cope with a BI response from PartySvc
}
