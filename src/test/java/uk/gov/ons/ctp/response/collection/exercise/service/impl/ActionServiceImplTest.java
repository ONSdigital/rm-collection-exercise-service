package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.response.action.representation.ActionDTO;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.collection.exercise.config.ActionSvc;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;

import java.net.URI;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActionServiceImplTest {

    private static final String ACTION_PATH = "/actions";

    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private ActionServiceImpl actionService;

    private RestTemplate restTemplate;

    @Mock
    private RestUtility restUtility;

    @Before
    public void before() {
        restTemplate = mock(RestTemplate.class);
        actionService = new ActionServiceImpl(restTemplate);
    }

    @Test
    public void testMakesRequest() {
        ActionSvc actionSvcConfig = new ActionSvc();
        actionSvcConfig.setActionsPath(ACTION_PATH);
        Mockito.when(appConfig.getActionSvc()).thenReturn(actionSvcConfig);

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(HTTP)
                .host(LOCALHOST)
                .port(80)
                .path(ACTION_PATH)
                .build();
        when(restUtility.createUriComponents(any(String.class), any(MultiValueMap.class))).
                thenReturn(uriComponents);

        ActionDTO actionDTO = new ActionDTO();
        actionService.createActionPlan("example name", "example description");

        URI uri = URI.create("http://google.com");

        verify(restTemplate).exchange(eq(uri), eq(HttpMethod.POST), any(), eq(ActionPlanDTO.class));
    }

}