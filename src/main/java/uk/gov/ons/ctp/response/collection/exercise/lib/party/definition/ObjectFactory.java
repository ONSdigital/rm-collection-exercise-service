package uk.gov.ons.ctp.response.collection.exercise.lib.party.definition;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each Java content interface and Java element interface
 * generated in the uk.gov.ons.ctp.response.collection.exercise.lib.party.definition package.
 *
 * <p>An ObjectFactory allows you to programatically construct new instances of the Java
 * representation for XML content. The Java representation of XML content can consist of schema
 * derived interfaces and classes representing the binding of schema type definitions, element
 * declarations and model groups. Factory methods for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

  /**
   * Create a new ObjectFactory that can be used to create new instances of schema derived classes
   * for package: uk.gov.ons.ctp.response.collection.exercise.lib.party.definition
   */
  public ObjectFactory() {}

  /** Create an instance of {@link PartyCreationRequestDTO } */
  public PartyCreationRequestDTO createPartyCreationRequestDTO() {
    return new PartyCreationRequestDTO();
  }

  /** Create an instance of {@link PartyCreationRequestAttributesDTO } */
  public PartyCreationRequestAttributesDTO createPartyCreationRequestAttributesDTO() {
    return new PartyCreationRequestAttributesDTO();
  }

  /** Create an instance of {@link SampleLinkCreationRequestDTO } */
  public SampleLinkCreationRequestDTO createSampleLinkCreationRequestDTO() {
    return new SampleLinkCreationRequestDTO();
  }

  /** Create an instance of {@link Association } */
  public Association createAssociation() {
    return new Association();
  }

  /** Create an instance of {@link Enrolment } */
  public Enrolment createEnrolment() {
    return new Enrolment();
  }
}
