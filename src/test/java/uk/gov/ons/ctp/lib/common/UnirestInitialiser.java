package uk.gov.ons.ctp.lib.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import java.io.IOException;

public class UnirestInitialiser {
  public static void initialise(final ObjectMapper mapper) {
    Unirest.setObjectMapper(
        new com.mashape.unirest.http.ObjectMapper() {
          public <T> T readValue(final String value, final Class<T> valueType) {
            try {
              return mapper.readValue(value, valueType);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }

          public String writeValue(final Object value) {
            try {
              return mapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
            }
          }
        });
  }
}
