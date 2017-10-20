package org.ditto.easyhan;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.examples.greeter.GreeterGrpc;
import io.grpc.examples.greeter.HelloReply;
import io.grpc.examples.greeter.HelloRequest;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;

import io.grpc.stub.StreamObserver;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.LocalManagementPort;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApplicationSpringBoot.class, TestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GreeterGrpcServiceTests {
    @LocalServerPort
    int randomServerPort;

    @LocalManagementPort
    int randomManagementPort;

    private ManagedChannel channel;

    @Value("${grpc.port}")
    int grpcPort;

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Autowired
    @Qualifier("globalInterceptor")
    private ServerInterceptor globalInterceptor;


    @Before
    public void setup() {
        channel = ManagedChannelBuilder.forAddress("localhost", grpcPort)
                .usePlaintext(true)
                .build();
    }

    @After
    public void tearDown() {
        channel.shutdown();
    }

    @Test
    public void simpleGreeting() throws ExecutionException, InterruptedException {


        String name = "John";
        final GreeterGrpc.GreeterFutureStub greeterFutureStub = GreeterGrpc.newFutureStub(channel);
        final HelloRequest helloRequest = HelloRequest.newBuilder().setName(name).build();
        final String reply = greeterFutureStub.sayHello(helloRequest).get().getMessage();
        assertNotNull(reply);
        assertTrue(String.format("Replay should contain name '%s'", name), reply.contains(name));

    }

    @Test
    public void interceptors() throws ExecutionException, InterruptedException {

        GreeterGrpc.newStub(channel)
                .sayHello(HelloRequest.newBuilder().setName("name").build(),
                        new StreamObserver<HelloReply>() {
                            @Override
                            public void onNext(HelloReply value) {

                            }

                            @Override
                            public void onError(Throwable t) {

                            }

                            @Override
                            public void onCompleted() {

                            }
                        });

        GreeterGrpc.newFutureStub(channel)
                .sayHello(HelloRequest.newBuilder().setName("name").build())
                .get().getMessage();

        GreeterGrpc.newBlockingStub(channel)
                .sayHello(HelloRequest.newBuilder().setName("name").build())
                .getMessage();


        // global interceptor should be invoked once on each service
        Mockito.verify(globalInterceptor, Mockito.times(3))
                .interceptCall(Mockito.any(), Mockito.any(), Mockito.any());


        // log interceptor should be invoked only on GreeterService and not CalculatorService
        outputCapture.expect(CoreMatchers.containsString(GreeterGrpc.METHOD_SAY_HELLO.getFullMethodName()));

    }



    @Test
    public void healthCheck() throws ExecutionException, InterruptedException {
        final HealthCheckRequest healthCheckRequest = HealthCheckRequest.newBuilder().setService(GreeterGrpc.getServiceDescriptor().getName()).build();
        final HealthGrpc.HealthFutureStub healthFutureStub = HealthGrpc.newFutureStub(channel);
        final HealthCheckResponse.ServingStatus servingStatus = healthFutureStub.check(healthCheckRequest).get().getStatus();
        assertNotNull(servingStatus);
        assertEquals(servingStatus, HealthCheckResponse.ServingStatus.SERVING);
    }
}
