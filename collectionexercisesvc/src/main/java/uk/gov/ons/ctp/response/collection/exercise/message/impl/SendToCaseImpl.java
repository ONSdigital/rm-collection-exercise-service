package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;
import uk.gov.ons.ctp.response.casesvc.definition.CaseCreation;
import uk.gov.ons.ctp.response.collection.exercise.message.SendToCase;

import javax.xml.datatype.DatatypeConfigurationException;
import java.text.ParseException;

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
		caseDTO.setActionPlanId("e71002ac-3575-47eb-b87f-cd9db92bf9a7");
		caseDTO.setPartyId("7bc5d41b-0549-40b3-ba76-42f6d4cf3992");
		caseDTO.setCollectionExerciseId("14fb3e68-4dca-46db-bf49-04b84e07e77c");
		caseDTO.setCollectionInstrumentId("7bc5d41b-0549-40b3-ba76-42f6d4cf3994");
		caseDTO.setSampleUnitRef("str1234");
		caseDTO.setSampleUnitType("B");
		
		log.info("Send to queue");
		rabbitTemplate.convertAndSend(caseDTO);
	}
}
