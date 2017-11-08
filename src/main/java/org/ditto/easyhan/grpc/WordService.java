package org.ditto.easyhan.grpc;

import com.google.gson.Gson;
import io.grpc.stub.StreamObserver;
import org.ditto.easyhan.model.Word;
import org.ditto.easyhan.repository.WordRepository;
import org.easyhan.word.grpc.ListRequest;
import org.easyhan.word.grpc.WordGrpc;
import org.easyhan.word.grpc.WordResponse;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.logging.Logger;

@GRpcService(interceptors = {LogInterceptor.class})
public class WordService extends WordGrpc.WordImplBase {
    private final static Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(WordService.class.getName());

    @Autowired
    private WordRepository wordRepository;

    @Override
    public void list(ListRequest request, StreamObserver<WordResponse> responseObserver) {
        logger.info(String.format("list start request=[%s]", gson.toJson(request)));
        List<Word> wordList = wordRepository.getAllBy(request.getLevel(), request.getLastUpdated(), new PageRequest(0, 100));
        if (wordList != null) {
            logger.info(String.format("ListRequest request=[%s]\n send imageList.size()=%d", gson.toJson(request), wordList.size()));
            for (Word word : wordList) {

                WordResponse response = WordResponse
                        .newBuilder()
                        .setWord(word.getWord())
                        .setIdx(word.getIdx())
                        .setPinyin1(word.getPinyin1())
                        .setPinyin2(word.getPinyin2())
                        .setStrokes(word.getStrokes())
                        .setLevel(word.getLevel())
                        .setCreated(word.getCreated())
                        .setLastUpdated(word.getLastUpdated())
                        .setVistCount(word.getVisitCount())
                        .build();
                responseObserver.onNext(response);

                logger.info(String.format("send word=[%s]", gson.toJson(word)));
            }
            responseObserver.onCompleted();
            logger.info(String.format("list end request=[%s]", gson.toJson(request)));
        }
    }
}