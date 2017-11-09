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
import org.ditto.easyhan.model.UserKey;
import org.ditto.easyhan.model.qq.QQOpenId;
import org.ditto.easyhan.model.qq.QQUserInfo;
import org.ditto.easyhan.repository.UserRepository;
import org.ditto.easyhan.service.QQService;
import org.ditto.sigin.grpc.QQSigninRequest;
import org.ditto.sigin.grpc.SigninGrpc;
import org.ditto.sigin.grpc.SigninResponse;
import org.easyhan.common.grpc.Error;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.SecretKey;
import java.util.logging.Logger;

@GRpcService(interceptors = {LogInterceptor.class})
public class SigninService extends SigninGrpc.SigninImplBase {
    private final static Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(SigninService.class.getName());
    @Autowired
    private QQService qqService;

    @Autowired
    private UserRepository userRepository;


    @Override
    public void qQSignin(QQSigninRequest request, StreamObserver<SigninResponse> responseObserver) {
        SigninResponse signinResponse = null;

        QQOpenId qqOpenId = qqService.getOpenId(request.getAccessToken());
        if (qqOpenId != null) {
            User user = userRepository.findOne(new UserKey(UserKey.IDIssuer.QQ, qqOpenId.openid));
            if (user == null) {
                user = newRegistrationUserWithQQ(qqOpenId);
            }
            if (user != null) {
                Claims claims = new DefaultClaims()
                        .setIssuer(UserKey.IDIssuer.QQ.name())
                        .setId(user.getId())
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
                        .setExpiresIn(String.format("%d", -1))
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
        } else {
            updateUserInfoWithQQ(qqOpenId.openid,request.getAccessToken());
        }
        responseObserver.onNext(signinResponse);
        responseObserver.onCompleted();
    }

    private void updateUserInfoWithQQ(String openid,String qqAccessToken) {
        QQUserInfo qqUserInfo = qqService.getQQUserInfo(qqAccessToken);
        UserKey userKey = new UserKey(UserKey.IDIssuer.QQ, openid);
        User user = userRepository.findOne(userKey);
        user.setNickname(qqUserInfo.nickname);
        userRepository.save(userKey,user);
    }

    private User newRegistrationUserWithQQ(QQOpenId qqOpenId) {
        return userRepository.save(
                new UserKey(
                        UserKey.IDIssuer.QQ, qqOpenId.openid),
                User.builder()
                        .setIdIssuer(UserKey.IDIssuer.QQ)
                        .setId(qqOpenId.openid)
                        .setCreated(System.currentTimeMillis())
                        .setLastUpdated(System.currentTimeMillis())
                        .build()
        );
    }

}