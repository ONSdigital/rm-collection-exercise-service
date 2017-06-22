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
    MultiValueMap<String, String> queryParams = new HttpHeaders();
    queryParams.add("searchString", searchString);

    String path = appConfig.getCollectionInstrumentSvc().getRequestCollectionInstruments() + "?searchString=" + searchString;
    UriComponents uriComponents = UriComponentsBuilder.newInstance()
        .scheme(appConfig.getCollectionInstrumentSvc().getConnectionConfig().getScheme())
        .host(appConfig.getCollectionInstrumentSvc().getConnectionConfig().getHost())
        .port(appConfig.getCollectionInstrumentSvc().getConnectionConfig().getPort())
        .path(path)
        .build()
        .encode();
    
    RestTemplate restTemplate = new RestTemplate();

    ResponseEntity<CollectionInstrumentDTO[]> responseEntity = restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, null, CollectionInstrumentDTO[].class);
//    return Arrays.asList(responseEntity.getBody());
            return collectionInstrumentSvcClientRestTemplate.getResources(path,
        CollectionInstrumentDTO[].class, null);
  }

  
  public static void main(String[] args) {
    
    String searchString = "{\"RU_REF\":\"0123456789\",\"COLLECTION_EXERCISE\":\"1c46f5aa-01d0-4300-aa1d-32aab8618760\"}";
    String path = "/collection-instrument-api/1.0.2/collectioninstrument";// + "?searchString=" + searchString;

    MultiValueMap<String, String> queryParams = new HttpHeaders();
    queryParams.add("searchString", URLEncoder.encode(searchString));
    
    UriComponents uriComponents = UriComponentsBuilder.newInstance()
        .scheme("http")
        .host("api-dev.apps.mvp.onsclofo.uk")
        .port("80")
        .path(path)
        .queryParams(queryParams)
        .build(true);
//        .encode();
    
    RestTemplate restTemplate = new RestTemplate();

    ResponseEntity<CollectionInstrumentDTO[]> responseEntity = restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, null, CollectionInstrumentDTO[].class);
    List<CollectionInstrumentDTO> asList = Arrays.asList(responseEntity.getBody());
    
  }
}
