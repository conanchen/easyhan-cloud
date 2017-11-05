package org.ditto.easyhan.grpc;

import com.google.gson.Gson;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.crypto.MacProvider;
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

import javax.crypto.SecretKey;
import java.util.UUID;
import java.util.logging.Logger;

@GRpcService(interceptors = {LogInterceptor.class})
public class SigninService extends SigninGrpc.SigninImplBase {
    private final static Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(SigninService.class.getName());
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
                user = newRegistrationUserWithQQ(qqOpenId, qqUserInfo);
            } else {
                user = userRepository.findOne(userQQ.getUserId());
            }
            if (user != null) {
                Claims claims = new DefaultClaims()
                        .setId(user.getUserId())
                        .setSubject(user.getNickname());
//                        .setExpiration(new Date(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000));
                SecretKey secretKey = MacProvider.generateKey();
                String jwt = Jwts.builder().setClaims(claims).compact();
                Jwt<Header, Claims> jwt1 = Jwts.parser().parseClaimsJwt(jwt);
                logger.info(String.format("secretKey.algorithm=%s,secretKey.format=%s,secretKey.encoded=%s,jwt1.id=%s",
                        secretKey.getAlgorithm(),
                        secretKey.getFormat(), new String(secretKey.getEncoded()), jwt1.getBody().getId()));
                signinResponse = SigninResponse
                        .newBuilder()
                        .setError(Error
                                .newBuilder()
                                .setCode("user.signin.ok")
                                .build())
                        .setAccessToken(jwt)
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

    private User newRegistrationUserWithQQ(QQOpenId qqOpenId, QQUserInfo qqUserInfo) {
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