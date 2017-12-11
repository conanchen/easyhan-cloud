package org.ditto.easyhan.grpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Utf8;
import com.google.gson.Gson;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.ditto.easyhan.model.Pinyin;
import org.ditto.easyhan.model.Word;
import org.ditto.easyhan.repository.WordRepository;
import org.easyhan.common.grpc.Error;
import org.easyhan.common.grpc.StatusResponse;
import org.easyhan.word.grpc.ListRequest;
import org.easyhan.word.grpc.UpdateRequest;
import org.easyhan.word.grpc.WordGrpc;
import org.easyhan.word.grpc.WordResponse;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@GRpcService(interceptors = {LogInterceptor.class})
public class WordService extends WordGrpc.WordImplBase {
    private final static Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(WordService.class.getName());
    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private WordRepository wordRepository;

    @Override
    public void list(ListRequest request, StreamObserver<WordResponse> responseObserver) {
        logger.info(String.format("list start request=[%s]", gson.toJson(request)));
        List<Word> wordList = wordRepository.getAllBy(request.getLevel(), request.getLastUpdated(), new PageRequest(0, 100));
        if (wordList != null) {
            logger.info(String.format("ListRequest request=[%s]\n send imageList.size()=%d", gson.toJson(request), wordList.size()));
            for (Word word : wordList) {
                List<org.easyhan.word.grpc.Pinyin> pinyins = new ArrayList<>();
                for (int i = 0; word.getPinyins() != null && i < word.getPinyins().size(); i++) {
                    Pinyin py = word.getPinyins().get(i);
                    pinyins.add(org.easyhan.word.grpc.Pinyin.newBuilder().setPinyin(py.pinyin).setMp3(py.mp3).build());
                }
                WordResponse response = WordResponse
                        .newBuilder()
                        .setWord(word.getWord())
                        .setLevel(word.getLevel())
                        .setLevelIdx(word.getLevelIdx())
                        .setCreated(word.getCreated())
                        .setLastUpdated(word.getLastUpdated())
                        .setVistCount(word.getVisitCount())
                        .addAllPinyins(pinyins)
                        .setRadical(StringUtils.defaultIfEmpty(word.getRadical(), ""))
                        .setWuxing(StringUtils.defaultIfEmpty(word.getWuxing(), ""))
                        .setTraditional(StringUtils.defaultIfEmpty(word.getTraditional(), ""))
                        .setWubi(StringUtils.defaultIfEmpty(word.getWubi(), ""))
                        .addAllStrokes15(word.getStrokes() == null ? new ArrayList() : word.getStrokes())
                        .addAllStrokenames(word.getStrokenames() == null ? new ArrayList() : word.getStrokenames())
                        .setStrokesCount17(word.getStrokes_count())
                        .setBasemean(StringUtils.defaultIfEmpty(word.getBasemean(), ""))
                        .setDetailmean(StringUtils.defaultIfEmpty(word.getDetailmean(), ""))
                        .addAllTerms(word.getTerms() == null ? new ArrayList<>() : word.getTerms())
                        .addAllRiddles(word.getRiddles() == null ? new ArrayList<>() : word.getRiddles())
                        .setFanyi(StringUtils.defaultIfEmpty(word.getFanyi(), ""))
                        .setBishun(StringUtils.defaultIfEmpty(word.getBishun(), ""))
                        .setDefined(BooleanUtils.toBooleanDefaultIfNull(word.getDefined(), false))
                        .build();
                responseObserver.onNext(response);

                logger.info(String.format("send word=[%s]", gson.toJson(word)));
            }
            responseObserver.onCompleted();
            logger.info(String.format("list end request=[%s]", gson.toJson(request)));
        }
    }

    @Override
    public void update(UpdateRequest request, StreamObserver<StatusResponse> responseObserver) {
        String html = request.getHtml();

        try {
//            mapper.writerWithDefaultPrettyPrinter()
//                    .writeValue();
            String fn = getWordFileName(request.getWord());
            org.apache.commons.io.FileUtils.writeStringToFile(new File(fn), html, StandardCharsets.UTF_8);
            StatusResponse statusResponse = StatusResponse
                    .newBuilder()
                    .setError(Error.newBuilder()
                            .setCode("update.fromhtml.ok")
                            .setDetails("update word detail from html")
                            .build())
                    .build();
            responseObserver.onNext(statusResponse);
            logger.info(String.format("update end save file=%s ,statusResponse=%s", fn,
                    gson.toJson(statusResponse)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
            responseObserver.onCompleted();
        }

    }

    private String getWordFileName(String word) {
        return
                String.format("/Users/mellychen/hiask/easyhan-cloud/src/main/resources/words/%x.html",
                        word.codePointAt(0));
    }

}