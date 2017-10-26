package org.ditto.easyhan.grpc;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Interceptor that validates user's identity. */
@Slf4j
@Component
class MyAuthInterceptor implements ServerInterceptor {
  public static final Context.Key<UserMe> USER_IDENTITY
          = Context.key("identity"); // "identity" is just for debugging

  @Override 
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call,
      Metadata headers,
      ServerCallHandler<ReqT, RespT> next) {
    // You need to implement validateIdentity 
    UserMe identity = validateIdentity(headers);
    if (identity == null) { // this is optional, depending on your needs
      // Assume user not authenticated 
      call.close(Status.UNAUTHENTICATED.withDescription("some more info"),
                 new Metadata());
      return new ServerCall.Listener() {}; 
    } 
    Context context = Context.current().withValue(USER_IDENTITY, identity);
    return Contexts.interceptCall(context, call, headers, next);
  }

  private UserMe validateIdentity(Metadata headers) {
    return new UserMe("conan");
  }
} 