package org.ditto.easyhan;

import com.google.gson.Gson;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.stub.StreamObserver;
import org.easyhan.myword.grpc.*;
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
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApplicationSpringBoot.class, TestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MyWordGrpcServiceTests {
    private final static Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(MyWordGrpcServiceTests.class.getName());
    @LocalServerPort
    int randomServerPort;

    @LocalManagementPort
    int randomManagementPort;

    private ManagedChannel channel;
    private MyWordGrpc.MyWordStub asyncStub;
    private MyWordGrpc.MyWordBlockingStub blockingStub;

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
        asyncStub = MyWordGrpc.newStub(channel);
        blockingStub = MyWordGrpc.newBlockingStub(channel);
    }

    @After
    public void tearDown() {
        channel.shutdown();
    }


    @Test
    public void interceptors() throws ExecutionException, InterruptedException {
        ListRequest listRequest = ListRequest.newBuilder().build()
                .newBuilder()
                .setLastUpdated(0l)
                .build();

        asyncStub.list(listRequest, new StreamObserver<MyWordResponse>() {
            @Override
            public void onNext(MyWordResponse value) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });

        blockingStub.list(listRequest).hasNext();
        MyWordResponse upsertResponse = blockingStub.upsert(UpsertRequest.newBuilder()
                .setWord("一")
                .build());

        // global interceptor should be invoked once on each service
        Mockito.verify(globalInterceptor, Mockito.times(3))
                .interceptCall(Mockito.any(), Mockito.any(), Mockito.any());


        // log interceptor should be invoked only on GreeterService and not CalculatorService
        outputCapture.expect(CoreMatchers.containsString(MyWordGrpc.METHOD_LIST.getFullMethodName()));
        outputCapture.expect(CoreMatchers.containsString(MyWordGrpc.METHOD_UPSERT.getFullMethodName()));
    }


    @Test
    public void healthCheck() throws ExecutionException, InterruptedException {
        final HealthCheckRequest healthCheckRequest = HealthCheckRequest.newBuilder().setService(MyWordGrpc.getServiceDescriptor().getName()).build();
        final HealthGrpc.HealthFutureStub healthFutureStub = HealthGrpc.newFutureStub(channel);
        final HealthCheckResponse.ServingStatus servingStatus = healthFutureStub.check(healthCheckRequest).get().getStatus();
        assertNotNull(servingStatus);
        assertEquals(servingStatus, HealthCheckResponse.ServingStatus.SERVING);
    }
}