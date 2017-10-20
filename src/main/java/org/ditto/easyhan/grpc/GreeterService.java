package org.ditto.easyhan.grpc;

import io.grpc.examples.greeter.GreeterGrpc;
import io.grpc.examples.greeter.HelloReply;
import io.grpc.examples.greeter.HelloRequest;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService(interceptors = {LogInterceptor.class})
public class GreeterService extends GreeterGrpc.GreeterImplBase {
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        final HelloReply.Builder replyBuilder = HelloReply.newBuilder().setMessage("Hello " + request.getName());
        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
    }
}