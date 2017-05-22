package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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

		caseDTO.setActionPlanId(1);
		caseDTO.setPartyId(1);
		caseDTO.setCollectionExerciseId(1);
		caseDTO.setCollectionInstrumentId(1);
		caseDTO.setSampleUnitRef("str1234");
		caseDTO.setSampleUnitType("B");
		
		
		log.info("Send to queue");
		rabbitTemplate.convertAndSend(caseDTO);
	}
}
