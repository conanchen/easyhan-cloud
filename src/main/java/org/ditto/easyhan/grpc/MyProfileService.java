package org.ditto.easyhan.grpc;

import com.google.gson.Gson;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import org.ditto.easyhan.model.User;
import org.ditto.easyhan.model.UserKey;
import org.ditto.easyhan.model.Word;
import org.ditto.easyhan.repository.UserRepository;
import org.ditto.easyhan.repository.WordRepository;
import org.easyhan.myprofile.grpc.GetRequest;
import org.easyhan.myprofile.grpc.MyProfileGrpc;
import org.easyhan.myprofile.grpc.MyProfileResponse;
import org.easyhan.word.grpc.ListRequest;
import org.easyhan.word.grpc.WordGrpc;
import org.easyhan.word.grpc.WordResponse;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.logging.Logger;

@GRpcService(interceptors = {MyAuthInterceptor.class, LogInterceptor.class})
public class MyProfileService extends MyProfileGrpc.MyProfileImplBase {
    private final static Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(MyProfileService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Override
    public void get(GetRequest request, StreamObserver<MyProfileResponse> responseObserver) {
        // Access to identity.
        Claims claims = MyAuthInterceptor.USER_CLAIMS.get();
        logger.info(String.format("get start claims.getId()=%s, request=[%s]", claims.getId(), gson.toJson(request)));

        User user = userRepository.findOne(new UserKey(UserKey.IDIssuer.valueOf(claims.getIssuer()),claims.getId()));
        if (user != null) {
            MyProfileResponse myProfileResponse = MyProfileResponse
                    .newBuilder()
                    .setNickName(user.getNickname())
                    .setAvartarUrl("")
                    .setUserNo("56789")
                    .setLastUpdated(user.getLastUpdated())
                    .build();
            responseObserver.onNext(myProfileResponse);

        }

        responseObserver.onCompleted();
    }
}