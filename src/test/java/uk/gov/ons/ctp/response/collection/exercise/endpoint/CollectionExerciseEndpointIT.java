package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CollectionExerciseEndpointIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void shouldReceiveSampleUploadedMessage(){
    }

}
