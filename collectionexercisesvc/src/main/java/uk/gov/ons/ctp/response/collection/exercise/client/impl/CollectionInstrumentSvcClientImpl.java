package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.collection.exercise.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collectionInstrument.representation.CollectionInstrumentDTO;

@Slf4j
@Component
public class CollectionInstrumentSvcClientImpl implements CollectionInstrumentSvcClient {

  @Autowired
  private AppConfig appConfig;

  @Autowired
  @Qualifier("collectionInstrumentSvc")
  public RestClient collectionInstrumentSvcClientRestTemplate;

  @Override
  public List<CollectionInstrumentDTO> requestCollectionInstruments(String searchString) {
    
    //temporarily not using RestClient from common due to collectionInstrument searchString requiring JSON
    
    MultiValueMap<String, String> queryParams = new HttpHeaders();
    queryParams.add("searchString", URLEncoder.encode(searchString));

    String path = appConfig.getCollectionInstrumentSvc().getRequestCollectionInstruments();
    UriComponents uriComponents = UriComponentsBuilder.newInstance()
        .scheme(appConfig.getCollectionInstrumentSvc().getConnectionConfig().getScheme())
        .host(appConfig.getCollectionInstrumentSvc().getConnectionConfig().getHost())
        .port(appConfig.getCollectionInstrumentSvc().getConnectionConfig().getPort())
        .path(path)
        .queryParams(queryParams)
        .build(true);

    RestTemplate restTemplate = new RestTemplate();

    log.debug("Enter getResources for path {}", path);
    ResponseEntity<CollectionInstrumentDTO[]> responseEntity = restTemplate.exchange(uriComponents.toUri(),
        HttpMethod.GET, null, CollectionInstrumentDTO[].class);
    return Arrays.asList(responseEntity.getBody());
  }

}
