package uk.gov.ons.ctp.response.collection.exercise.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import uk.gov.ons.ctp.response.collection.exercise.lib.collection.instrument.representation.CollectionInstrumentDTO;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CachingTestIT {

  @Autowired private CollectionInstrumentSvcClient collectionInstrumentSvcClient;
  @ClassRule public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
  private static final Logger log = LoggerFactory.getLogger(CachingTestIT.class);
  @ClassRule
  public static final EnvironmentVariables environmentVariables =
    new EnvironmentVariables().set("PUBSUB_EMULATOR_HOST", "127.0.0.1:18681");

  @Rule public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @ClassRule
  public static WireMockClassRule wireMockRule =
      new WireMockClassRule(options().port(18002).bindAddress("localhost"));

  private String searchString = "SURVEY_ID:cb8accda-6118-4d3b-85a3-149e28960c54";

  @Test
  public void testCache() throws Exception {
    StubMapping stubMapping =
        createCollectionInstrumentStub("CachingTestIT.CollectionInstrumentDTO.json");

    List<CollectionInstrumentDTO> CI =
        collectionInstrumentSvcClient.requestCollectionInstruments(searchString);
    assertEquals("cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87", CI.get(0).getSurveyId());

    removeStub(stubMapping);
    stubMapping = createCollectionInstrumentStub("CachingTestIT.CollectionInstrumentDTO2.json");
    List<CollectionInstrumentDTO> CI2 =
        collectionInstrumentSvcClient.requestCollectionInstruments(searchString);
    assertEquals("cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87", CI2.get(0).getSurveyId());
    assertNotEquals("cb0711c3-0ac8-41d3-ae0e-567e5ea1ef88", CI2.get(0).getSurveyId());
    removeStub(stubMapping);
  }

  @Test
  public void testCacheEvict() throws Exception {

    StubMapping stubMapping =
        createCollectionInstrumentStub("CachingTestIT.CollectionInstrumentDTO.json");
    List<CollectionInstrumentDTO> CI =
        collectionInstrumentSvcClient.requestCollectionInstruments(searchString);
    assertEquals("cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87", CI.get(0).getSurveyId());

    removeStub(stubMapping);
    stubMapping = createCollectionInstrumentStub("CachingTestIT.CollectionInstrumentDTO2.json");
    List<CollectionInstrumentDTO> CI2 =
        collectionInstrumentSvcClient.requestCollectionInstruments(searchString);
    assertEquals("cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87", CI2.get(0).getSurveyId());
    assertNotEquals("cb0711c3-0ac8-41d3-ae0e-567e5ea1ef88", CI2.get(0).getSurveyId());
    Thread.sleep(61000);

    removeStub(stubMapping);
    stubMapping = createCollectionInstrumentStub("CachingTestIT.CollectionInstrumentDTO2.json");
    List<CollectionInstrumentDTO> CI3 =
        collectionInstrumentSvcClient.requestCollectionInstruments(searchString);
    assertEquals("cb0711c3-0ac8-41d3-ae0e-567e5ea1ef88", CI3.get(0).getSurveyId());
    assertNotEquals("cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87", CI3.get(0).getSurveyId());
    removeStub(stubMapping);
  }

  private StubMapping createCollectionInstrumentStub(String resourceName) throws IOException {
    String json = loadResourceAsString(CachingTestIT.class, resourceName);
    return stubFor(
        get(urlMatching(
                "\\/collection-instrument-api\\/1.0.2\\/collectioninstrument\\?searchString=.*"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }

  private String loadResourceAsString(Class clazz, String resourceName) throws IOException {
    InputStream is = clazz.getResourceAsStream(resourceName);
    StringWriter writer = new StringWriter();
    IOUtils.copy(is, writer, StandardCharsets.UTF_8.name());
    return writer.toString();
  }
}
