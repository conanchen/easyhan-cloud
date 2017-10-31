package org.ditto.easyhan.grpc;

import io.grpc.stub.StreamObserver;
import org.ditto.easyhan.model.User;
import org.ditto.easyhan.model.qq.QQOpenId;
import org.ditto.easyhan.model.qq.QQUserInfo;
import org.ditto.easyhan.model.qq.UserQQ;
import org.ditto.easyhan.model.qq.UserQQKey;
import org.ditto.easyhan.repository.UserQQRepository;
import org.ditto.easyhan.repository.UserRepository;
import org.ditto.easyhan.service.QQService;
import org.ditto.sigin.grpc.QQSigninRequest;
import org.ditto.sigin.grpc.SigninGrpc;
import org.ditto.sigin.grpc.SigninResponse;
import org.easyhan.common.grpc.Error;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@GRpcService(interceptors = {LogInterceptor.class})
public class SigninService extends SigninGrpc.SigninImplBase {
    @Autowired
    private QQService qqService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserQQRepository userQQRepository;


    @Override
    public void qQSignin(QQSigninRequest request, StreamObserver<SigninResponse> responseObserver) {
        SigninResponse signinResponse = null;

        QQOpenId qqOpenId = qqService.getOpenId(request.getAccessToken());
        if (qqOpenId != null) {
            User user = null;
            UserQQ userQQ = userQQRepository.findOne(new UserQQKey(qqOpenId.openid));
            if (userQQ == null) {
                QQUserInfo qqUserInfo = qqService.getQQUserInfo(request.getAccessToken());
                user = registerUserWithQQ(qqOpenId, qqUserInfo);
            } else {
                user = userRepository.findOne(userQQ.getUserId());
            }
            if (user != null) {
                signinResponse = SigninResponse
                        .newBuilder()
                        .setError(Error
                                .newBuilder()
                                .setCode("user.signin.ok")
                                .build())
                        .setAccessToken(user.getAccessToken())
                        .setExpiresIn(String.format("%d", user.getExpiresIn()))
                        .build();
            }
        }

        if (signinResponse == null) {
            signinResponse = SigninResponse
                    .newBuilder()
                    .setError(Error
                            .newBuilder()
                            .setCode("user.signin.error")
                            .build())
                    .build();
        }
        responseObserver.onNext(signinResponse);
        responseObserver.onCompleted();
    }

    private User registerUserWithQQ(QQOpenId qqOpenId, QQUserInfo qqUserInfo) {
        String userId = UUID.randomUUID().toString();
        UserQQKey userQQKey = new UserQQKey(qqOpenId.openid);
        UserQQ userQQ = userQQRepository.findOne(userQQKey);
        if (userQQ == null) {
            userQQ = userQQRepository.save(userQQKey, UserQQ
                    .builder()
                    .setUserId(userId)
                    .setClient_id(qqOpenId.client_id)
                    .setOpenid(qqOpenId.openid)
                    .setNickname(qqUserInfo.nickname)
                    .setCity(qqUserInfo.city)
                    .setProvince(qqUserInfo.province)
                    .setCreated(System.currentTimeMillis())
                    .setLastUpdated(System.currentTimeMillis())
                    .build());
        }
        if (userQQ != null) {
            User user = userRepository.save(userId, User
                    .builder()
                    .setUserId(userId)
                    .setNickname(userQQ.getNickname())
                    .setCreated(System.currentTimeMillis())
                    .setLastUpdated(System.currentTimeMillis())
                    .setAccessToken(UUID.randomUUID().toString())
                    .setExpiresIn(-1)
                    .build());
            return user;
        }
        return null;
    }

}