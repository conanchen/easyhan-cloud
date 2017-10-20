package org.ditto.easyhan;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import org.junit.*;
import org.junit.runner.RunWith;
import org.lognet.springboot.grpc.GRpcServerBuilderConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.LocalManagementPort;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.Test;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApplicationSpringBoot.class, TestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationSpringBootTests {
    @LocalServerPort
    int randomServerPort;

    @LocalManagementPort
    int randomManagementPort;



    private ManagedChannel channel;

    @Rule
    public OutputCapture outputCapture = new OutputCapture();


    @Autowired
    private ApplicationContext context;


    @Before
    public void setup() {
        channel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .usePlaintext(true)
                .build();
    }

    @After
    public void tearDown() {
        channel.shutdown();
    }


    @Test
    public void actuatorTest() throws ExecutionException, InterruptedException {
        final TestRestTemplate template = new TestRestTemplate();

        ResponseEntity<String> response = template.getForEntity(String.format("http://localhost:%d/env",randomServerPort), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    public void testDefaultConfigurer() {
        Assert.assertEquals("Default configurer should be picked up",
                context.getBean(GRpcServerBuilderConfigurer.class).getClass(),
                GRpcServerBuilderConfigurer.class);
    }
}
