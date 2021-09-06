package uk.gov.ons.ctp.response.collection.exercise.client;

import static org.mockito.ArgumentMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtility;

@RunWith(MockitoJUnitRunner.class)
public class CaseSvcClientTest {

  private UUID collectionExerciseId;

  @InjectMocks private CaseSvcClient client;

  @Mock private RestUtility restUtility;

  @Mock private RestTemplate restTemplate;

  @Mock private AppConfig appConfig;

  @Mock private ObjectMapper objectMapper;

  @Before
  public void setup() {
    collectionExerciseId = UUID.randomUUID();
  }

  //  @Test
  //  public void testGetNumberOfCases() throws JsonProcessingException {
  //    CaseSvc caseSvc = new CaseSvc();
  //    UriComponents uriComponents =
  //        UriComponentsBuilder.newInstance()
  //            .path(caseSvc.getNumberOfCasesPath())
  //            .queryParams(null)
  //            .build();
  //
  //    Long output = 10L;
  //    CaseSvc casesvc = mock(CaseSvc.class);
  //
  //    when(appConfig.getCaseSvc()).thenReturn(casesvc);
  //    when(restUtility.createUriComponents(any(), any(), any())).thenReturn(uriComponents);
  //    when(restUtility.createHttpEntity(isNull())).thenReturn(mock(HttpEntity.class));
  //
  //    ResponseEntity<String> responseEntity =
  //        new ResponseEntity(String.valueOf(output), HttpStatus.OK);
  //    when(restTemplate.exchange(
  //            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
  //        .thenReturn(responseEntity);
  //
  //    Long result = client.getNumberOfCases(collectionExerciseId);
  //    assertThat(result).isEqualTo(output);
  //  }
}
