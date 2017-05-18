package uk.gov.ons.ctp.response.collection.exercise.message;

import java.text.ParseException;

import javax.xml.datatype.DatatypeConfigurationException;

public interface SendToCase {
	  /**
	   * To publish a caseReceipt to queue
	   * @param partyDTO to be sent
	 * @throws ParseException 
	 * @throws DatatypeConfigurationException 
	   */
	   void send() throws DatatypeConfigurationException, ParseException;
}
