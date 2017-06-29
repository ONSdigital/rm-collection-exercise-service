package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.collection.exercise.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.instrument.representation.CollectionInstrumentDTO;

/**
 * HTTP RestClient implementation for calls to the CollectionInstrument service
 *
 */
@Component
public class CollectionInstrumentSvcClientImpl implements CollectionInstrumentSvcClient {

  @Autowired
  private AppConfig appConfig;

  @Autowired
  @Qualifier("collectionInstrumentSvc")
  private RestClient collectionInstrumentSvcClientRestTemplate;

  @Override
  public List<CollectionInstrumentDTO> requestCollectionInstruments(String searchString) {

    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add("searchString", "{searchStringValue}");

    String path = appConfig.getCollectionInstrumentSvc().getRequestCollectionInstruments();

    List<CollectionInstrumentDTO> collectionInstrumentDTOList = collectionInstrumentSvcClientRestTemplate
        .getResourcesWithJsonParam(path, CollectionInstrumentDTO[].class, null, queryParams, searchString);

    return collectionInstrumentDTOList;
  }

}
