package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.response.casesvc.definition.CaseCreation;
import uk.gov.ons.ctp.response.collection.exercise.message.SendToCase;

/**
 * The publisher to queues
 */
@Slf4j
@MessageEndpoint
public class SendToCaseImpl implements SendToCase {

	@Qualifier("caseRabbitTemplate")
	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public void send() throws DatatypeConfigurationException, ParseException {

		CaseCreation caseDTO = new CaseCreation();
		//Case Data

		//TODO Integers fixed to Strings to temporarily allow to build.
		caseDTO.setActionPlanId("7bc5d41b-0549-40b3-ba76-42f6d4cf3991");
		caseDTO.setPartyId("7bc5d41b-0549-40b3-ba76-42f6d4cf3992");
		caseDTO.setCollectionExerciseId("7bc5d41b-0549-40b3-ba76-42f6d4cf3993");
		caseDTO.setCollectionInstrumentId("7bc5d41b-0549-40b3-ba76-42f6d4cf3994");
		caseDTO.setSampleUnitRef("str1234");
		caseDTO.setSampleUnitType("B");
		
		log.info("Send to queue");
		rabbitTemplate.convertAndSend(caseDTO);
	}
}
