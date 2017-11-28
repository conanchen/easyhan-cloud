package org.ditto.easyhan.grpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.grpc.stub.StreamObserver;
import org.apache.commons.collections.ListUtils;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.io.File;
import java.io.IOException;
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
                        .setStrokesCount16(word.getStrokes_count())
                        .setBasemean(StringUtils.defaultIfEmpty(word.getBasemean(), ""))
                        .setDetailmean(StringUtils.defaultIfEmpty(word.getDetailmean(), ""))
                        .addAllTerms(word.getTerms() == null ? new ArrayList<>() : word.getTerms())
                        .addAllRiddles(word.getRiddles() == null ? new ArrayList<>() : word.getRiddles())
                        .setFanyi(StringUtils.defaultIfEmpty(word.getFanyi(), ""))
                        .setBishun(StringUtils.defaultIfEmpty(word.getBishun(), ""))

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
        Document doc = Jsoup.parse(html);
        WordBaidu wordBaidu = new WordBaidu();
        wordBaidu.word = request.getWord();
        wordBaidu.pinyins = tone_py(doc);
        wordBaidu.radical = radical(doc);
        wordBaidu.wuxing = wuxing(doc);
        wordBaidu.traditional = traditional(doc);
        wordBaidu.wubi = wubi(doc);
        wordBaidu.strokes = stroke(doc);
        wordBaidu.strokes_count = wordBaidu.strokes.size();
        wordBaidu.basemean = basemean(doc);
        wordBaidu.detailmean = detailmean(doc);
        wordBaidu.terms = term(doc);
        wordBaidu.riddles = riddle(doc);
        wordBaidu.fanyi = fanyi(doc);
        wordBaidu.bishun = word_bishun(doc);

        upsertWordWithBaidu(request.getWord(), wordBaidu);

        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(getWordFileName(wordBaidu.word)), wordBaidu);
            StatusResponse statusResponse = StatusResponse.newBuilder().setError(Error.newBuilder().setCode("update.fromhtml.ok").setDetails("update word detail from html").build()).build();
            responseObserver.onNext(statusResponse);
            logger.info(String.format("update end wordBaidu=[%s],statusResponse=%s", gson.toJson(wordBaidu), gson.toJson(statusResponse)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
            responseObserver.onCompleted();
        }

    }

    public void upsertWordWithBaidu(String word, WordBaidu wordBaidu) {
        Word wordObj = wordRepository.findOne(word);

        if (wordObj != null) {
            Word w = Word
                    .builder()
                    .setWord(word)
                    .setLevel(wordObj.getLevel())
                    .setLevelIdx(wordObj.getLevelIdx())
                    .setCreated(wordObj.getCreated())
                    .setLastUpdated(System.currentTimeMillis())
                    .setVisitCount(wordObj.getVisitCount())
                    .setPinyins(wordBaidu.pinyins)
                    .setRadical(wordBaidu.radical)
                    .setWuxing(wordBaidu.wuxing)
                    .setTraditional(wordBaidu.traditional)
                    .setWubi(wordBaidu.wubi)
                    .setStrokes(wordBaidu.strokes)
                    .setStrokes_count(wordBaidu.strokes_count)
                    .setBasemean(wordBaidu.basemean)
                    .setDetailmean(wordBaidu.detailmean)
                    .setTerms(wordBaidu.terms)
                    .setRiddles(wordBaidu.riddles)
                    .setFanyi(wordBaidu.fanyi)
                    .setBishun(wordBaidu.bishun)

                    .build();
            wordRepository.save(word, w);
        }
    }


    private String getWordFileName(String word) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            sb.append("u" + Integer.toHexString(c));
        }
        return
                String.format("/Users/mellychen/hiask/easyhan-cloud/src/main/resources/words/%s.json",
                        sb.toString());
    }

    private List<Pinyin> tone_py(Document doc) {
        List<Pinyin> result = new ArrayList<>();
        try {
            Element e = doc.getElementById("pinyin");
            Elements spans = e.getElementsByTag("span");
            for (int i = 0; spans != null && i < spans.size(); i++) {
                try {
                    String pinyin = spans.get(i).getElementsByTag("b").get(0).childNode(0).outerHtml();
                    String mp3 = spans.get(i).getElementsByTag("a").get(0).attr("url");
                    result.add(new Pinyin(pinyin, mp3));
                } catch (Exception e1) {
                    e1.printStackTrace();
                } finally {

                }
            }
            logger.info(String.format("pinyins=%s", gson.toJson(result)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

        return result;
    }

    private String radical(Document doc) {
        String result = "";
        try {
            result = doc.getElementById("radical").child(1).html();
            logger.info(String.format("radical=%s", result));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return result;
    }


    private String wuxing(Document doc) {
        String result = "";
        try {
            result = doc.getElementById("wuxing").child(1).html();
            logger.info(String.format("wuxing=%s", result));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return result;
    }

    private String traditional(Document doc) {
        String result = "";
        try {
            result = doc.getElementById("traditional").child(1).html();
            logger.info(String.format("traditional=%s", result));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return result;
    }


    private String wubi(Document doc) {
        String result = "";
        try {
            result = doc.getElementById("wubi").child(1).html();
            logger.info(String.format("wubi=%s", result));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return result;
    }


    private List<String> stroke(Document doc) {
        List<String> result = new ArrayList<>();
        try {
            String strokes = doc.getElementById("stroke").childNode(1).outerHtml();
            String[] strokeArr = StringUtils.splitByWholeSeparator(StringUtils.remove(strokes, '\u00a0').trim(), "、");
            for (int j = 0; j < strokeArr.length; j++) {
                if (StringUtils.isNotEmpty(strokeArr[j])) {
                    result.add(StringUtils.trim(strokeArr[j]));
                }
            }
            logger.info(String.format("stroke=%s", gson.toJson(result)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

        return result;
    }


    private String basemean(Document doc) {
        String result = "";
        try {
            result = doc.getElementById("base-mean").html();
            logger.info(String.format("basemean=%s", result));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return result;
    }


    private String detailmean(Document doc) {
        String result = "";
        try {
            result = doc.getElementById("detail-mean").html();
            logger.info(String.format("detailmean=%s", result));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return result;
    }


    private List<String> term(Document doc) {
        List<String> result = new ArrayList<>();
        try {
            Elements elements = doc.getElementById("term").getElementsByTag("a");
            for (int i = 0; elements != null && i < elements.size(); i++) {
                result.add(elements.get(i).html());
            }
            logger.info(String.format("term=%s", gson.toJson(result)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return result;
    }


    private List<String> riddle(Document doc) {
        List<String> result = new ArrayList<>();
        try {
            Elements elements = doc.getElementById("riddle-wrapper").getElementsByTag("p");
            for (int i = 0; elements != null && i < elements.size(); i++) {
                result.add(elements.get(i).html());
            }
            logger.info(String.format("riddle=%s", gson.toJson(result)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

        return result;
    }

    private String fanyi(Document doc) {
        String result = "";
        try {
            result = doc.getElementById("fanyi-wrapper").getElementsByTag("p").get(0).html();
            logger.info(String.format("fanyi=%s", result));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

        return result;
    }


    private String word_bishun(Document doc) {
        String result = "";
        try {
            result = doc.getElementById("word_bishun").attr("data-src");
            logger.info(String.format("word_bishun=%s", result));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

        return result;
    }


}