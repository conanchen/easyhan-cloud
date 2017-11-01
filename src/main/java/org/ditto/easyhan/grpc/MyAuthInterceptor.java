package org.ditto.easyhan.grpc;

import com.google.gson.Gson;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Interceptor that validates user's identity.
 */
@Slf4j
@Component
class MyAuthInterceptor implements ServerInterceptor {
    private final static Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(MyAuthInterceptor.class.getName());


    public static final Context.Key<UserMe> USER_IDENTITY
            = Context.key("identity"); // "identity" is just for debugging
    private static final Metadata.Key<String> AUTHORIZATION = Metadata.Key.of("authorization",
            Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<byte[]> EXTRA_AUTHORIZATION = Metadata.Key.of(
            "Extra-Authorization-bin", Metadata.BINARY_BYTE_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String it = headers.get(AUTHORIZATION);
        logger.info(String.format("authorization=%s", it));
        // You need to implement validateIdentity
        UserMe identity = validateIdentity(headers);
        if (identity == null) { // this is optional, depending on your needs
            // Assume user not authenticated
            call.close(Status.UNAUTHENTICATED.withDescription("some more info"),
                    new Metadata());
            return new ServerCall.Listener() {
            };
        }
        Context context = Context.current().withValue(USER_IDENTITY, identity);
        return Contexts.interceptCall(context, call, headers, next);
    }

    private UserMe validateIdentity(Metadata headers) {
        return new UserMe("conan");
    }
} 