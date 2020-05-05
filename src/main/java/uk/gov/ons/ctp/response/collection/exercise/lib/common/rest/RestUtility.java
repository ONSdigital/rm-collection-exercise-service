package uk.gov.ons.ctp.response.collection.exercise.lib.common.rest;

import java.nio.charset.Charset;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/** Utility for REST calls */
@Component
public class RestUtility {

  private RestUtilityConfig config;

  /** Construct with no details of the server - will use the default provided by RestClientConfig */
  public RestUtility() {
    this.config = new RestUtilityConfig();
  }

  /**
   * Construct with the core details of the server
   *
   * @param clientConfig the configuration
   */
  public RestUtility(RestUtilityConfig clientConfig) {
    this.config = clientConfig;
  }

  /**
   * Create Uri Components
   *
   * @param path for uri
   * @param queryParams for uri
   * @param pathParams for uri
   * @return UriComponents from supplied path, queryParams and pathParams
   */
  public UriComponents createUriComponents(
      String path, MultiValueMap<String, String> queryParams, Object... pathParams) {
    UriComponents uriComponentsWithOutQueryParams =
        UriComponentsBuilder.newInstance()
            .scheme(config.getScheme())
            .host(config.getHost())
            .port(config.getPort())
            .path(path)
            .buildAndExpand(pathParams);

    // Have to build UriComponents for query parameters separately as Expand interprets braces in
    // JSON query string
    // values as URI template variables to be replaced.

    return UriComponentsBuilder.newInstance()
        .uriComponents(uriComponentsWithOutQueryParams)
        .queryParams(queryParams)
        .build()
        .encode();
  }

  /**
   * Creates a {@link HttpEntity} with basic auth header and application/json set for Content-Type
   * and Accept
   *
   * @param <H> generic passed in for body content
   * @return HttpEntity containing object as JSON
   */
  public <H> HttpEntity<H> createHttpEntityWithAuthHeader() {
    return new HttpEntity<>(getHttpHeaders());
  }

  /**
   * Creates a {@link HttpEntity} with basic authentication header and application/json set for
   * Content-Type and Accept
   *
   * @param entity entity to be created from
   * @param <H> generic passed in for body content
   * @return HttpEntity containing object as JSON
   */
  public <H> HttpEntity<H> createHttpEntity(H entity) {
    return new HttpEntity<>(entity, getHttpHeaders());
  }

  private HttpHeaders getHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    if (this.config.getUsername() != null && this.config.getPassword() != null) {
      String auth = this.config.getUsername() + ":" + this.config.getPassword();
      byte[] encodedAuth = Base64.encode(auth.getBytes(Charset.forName("US-ASCII")));
      String authHeader = "Basic " + new String(encodedAuth);
      headers.set("Authorization", authHeader);
    }
    return headers;
  }
}
