package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.collection.exercise.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collectionInstrument.representation.CollectionInstrumentDTO;

@Component
public class CollectionInstrumentSvcClientImpl implements CollectionInstrumentSvcClient {

  @Autowired
  private AppConfig appConfig;

  @Autowired
  @Qualifier("collectionInstrumentSvc")
  public RestClient collectionInstrumentSvcClientRestTemplate;

  @Override
  public List<CollectionInstrumentDTO> requestCollectionInstruments(String searchString) {
    return collectionInstrumentSvcClientRestTemplate.getResources(
        appConfig.getCollectionInstrumentSvc().getRequestCollectionInstruments(),
        CollectionInstrumentDTO[].class, searchString);
  }

}
