package org.ditto.easyhan.grpc;

import com.google.gson.Gson;
import io.grpc.stub.StreamObserver;
import org.ditto.easyhan.model.UserWord;
import org.ditto.easyhan.model.UserWordKey;
import org.ditto.easyhan.repository.Constants;
import org.ditto.easyhan.repository.UserWordRepository;
import org.easyhan.common.grpc.Error;
import org.easyhan.myword.grpc.*;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.logging.Logger;

@GRpcService(interceptors = {LogInterceptor.class})
public class MyWordService extends MyWordGrpc.MyWordImplBase {
    private final static Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(MyWordService.class.getName());

    @Autowired
    private UserWordRepository userWordRepository;

    @Override
    public void list(ListRequest request, StreamObserver<MyWordResponse> responseObserver) {
        logger.info(String.format("list start request=[%s]", gson.toJson(request)));
        List<UserWord> userWordList = userWordRepository.getAllBy(Constants.TEST_USERID, request.getLastUpdated(), new PageRequest(0, 100));
        if (userWordList != null) {
            logger.info(String.format("list send userWordList.size()=%d", userWordList.size()));
            for (UserWord userWord : userWordList) {

                MyWordResponse response = MyWordResponse
                        .newBuilder()
                        .setWord(userWord.getWord())
                        .setMemIdx(userWord.getMemIdx())
                        .setLastUpdated(userWord.getLastUpdated())
                        .build();
                responseObserver.onNext(response);

                logger.info(String.format("send userWord=[%s]", gson.toJson(userWord)));
            }
            responseObserver.onCompleted();
            logger.info(String.format("list end request=[%s]", gson.toJson(request)));
        }
    }

    @Override
    public void upsert(UpsertRequest request, StreamObserver<UpsertResponse> responseObserver) {
        logger.info(String.format("upsert start request=[%s]", gson.toJson(request)));
        UserWordKey userWordKey = new UserWordKey(Constants.TEST_USERID, request.getWord());
        UserWord userWord = userWordRepository.findOne(userWordKey);
        if (userWord != null) {
            userWord.setMemIdx(userWord.getMemIdx() + 1);
            userWord.setLastUpdated(System.currentTimeMillis());
        } else {
            userWord = UserWord.builder()
                    .setUserId(Constants.TEST_USERID)
                    .setWord(request.getWord())
                    .setMemIdx(1)
                    .setCreated(System.currentTimeMillis())
                    .setLastUpdated(System.currentTimeMillis())
                    .build();
        }
        userWord = userWordRepository.save(userWordKey, userWord);

        UpsertResponse upsertResponse = UpsertResponse.newBuilder()
                .setError(Error.newBuilder().setCode("myword.upsert.ok").build())
                .setMemIdx(userWord.getMemIdx())
                .build();
        responseObserver.onNext(upsertResponse);
        logger.info(String.format("upsert send upsertResponse=[%s]", gson.toJson(upsertResponse)));
        responseObserver.onCompleted();
        logger.info(String.format("upsert end request=[%s]", gson.toJson(request)));
    }
}