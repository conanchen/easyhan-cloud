package org.ditto.easyhan.data;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ignite.Ignite;
import org.ditto.easyhan.grpc.WordBaidu;
import org.ditto.easyhan.model.Pinyin;
import org.ditto.easyhan.model.Word;
import org.ditto.easyhan.repository.WordRepository;
import org.easyhan.common.grpc.HanziLevel;
import org.easyhan.word.HanZi;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * 服务启动执行
 */
@Component
@Slf4j
public class WordDataImportRunner implements CommandLineRunner {
    private static final Logger logger = Logger.getLogger(WordDataImportRunner.class.getName());
    private final static Gson gson = new Gson();
    @Autowired
    private Ignite ignite;


    @Autowired
    private WordRepository wordRepository;


    @Override
    public void run(String... args) throws Exception {
        log.info("Start SexyImageDataImportRunner 服务启动执行，执行Image数据导入");

        int oneSize = saveWords(HanZi.LEVEL1, HanziLevel.ONE, 1);
        int twoSize = saveWords(HanZi.LEVEL2, HanziLevel.TWO, oneSize + 1);
        int threeSize = saveWords(HanZi.LEVEL3, HanziLevel.THREE, oneSize + twoSize + 1);

        log.info(String.format("End   SexyImageDataImportRunner 服务启动执行，执行Image数据导入\n " +
                "oneSize=%d,twoSize=%d,threeSize=%d", oneSize, twoSize, threeSize));

//        checkStrokeDefinitions();
//        checkWordStrokNameDefined(HanZi.LEVEL1);
//        checkWordStrokNameDefined(HanZi.LEVEL2);
//        checkWordStrokNameDefined(HanZi.LEVEL3);

    }

    private int saveWords(String[] words, HanziLevel level, int startIdx) {
        for (int i = 0; i < words.length; i++) {
            int newIdx = i + startIdx;
            String word = words[i];
            upsertWordWithLevelInfo(word, level, newIdx);
            updateWordFromBaiduHtmlFile(word, newIdx);
            try {
                //Ensure every word's lastUpdated different
                Thread.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info(String.format("done save %d words", words.length));
        return words.length;
    }


    private void upsertWordWithLevelInfo(String word, HanziLevel level, int newIdx) {
        Word wordObj = wordRepository.findOne(word);
        if (wordObj == null) {
            wordObj = Word.builder()
                    .setWord(word)
                    .setLevel(level)
                    .setLevelIdx(newIdx)
                    .setCreated(System.currentTimeMillis())
                    .setLastUpdated(System.currentTimeMillis())
                    .setVisitCount(0)
                    .build();
            wordRepository.save(word, wordObj);
            log.info(String.format("new %04d,%s:%x", newIdx, word, word.codePointAt(0)));
        } else {
            if (level.compareTo(wordObj.getLevel()) != 0
                    || newIdx != wordObj.getLevelIdx()
                    ) {
                log.info(String.format("upd %04d,%s:%x", newIdx, word, word.codePointAt(0)));
                wordObj.setLevel(level);
                wordObj.setLevelIdx(newIdx);
                wordObj.setLastUpdated(System.currentTimeMillis());
                wordRepository.save(word, wordObj);
            }
        }
    }


    @NotNull
    private void updateWordFromBaiduHtmlFile(String word, int newIdx) {
        Word wordObj = wordRepository.findOne(word);
        if (wordObj != null) {

            //Get file from resources folder
            ClassLoader classLoader = getClass().getClassLoader();
            URL res = classLoader.getResource(String.format("words/%x.html", word.codePointAt(0)));
            if (res != null) {
                File file = new File(res.getFile());
                try {
                    String html = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                    BasicFileAttributes attr = Files.readAttributes(Paths.get(file.getPath()), BasicFileAttributes.class,
                            LinkOption
                                    .NOFOLLOW_LINKS);
                    if (attr.lastModifiedTime().toMillis() > wordObj.getDefineCreated()) {
                        Document doc = Jsoup.parse(html);
                        Element e = doc.getElementById("results");
                        if (e != null) {
                            WordBaidu wordBaidu = new WordBaidu();
                            wordBaidu.word = word;
                            wordBaidu.pinyins = BaiduHanZiUtil.tone_py(doc);
                            wordBaidu.radical = BaiduHanZiUtil.radical(doc);
                            wordBaidu.wuxing = BaiduHanZiUtil.wuxing(doc);
                            wordBaidu.traditional = BaiduHanZiUtil.traditional(doc);
                            wordBaidu.wubi = BaiduHanZiUtil.wubi(doc);
                            wordBaidu.strokes = BaiduHanZiUtil.strokes(doc);
                            wordBaidu.strokenames = BaiduHanZiUtil.strokenames(doc);
                            wordBaidu.strokes_count = wordBaidu.strokenames.size();
                            wordBaidu.basemean = BaiduHanZiUtil.basemean(doc);
                            wordBaidu.detailmean = BaiduHanZiUtil.detailmean(doc);
                            wordBaidu.terms = BaiduHanZiUtil.term(doc);
                            wordBaidu.riddles = BaiduHanZiUtil.riddle(doc);
                            wordBaidu.fanyi = BaiduHanZiUtil.fanyi(doc);
                            wordBaidu.bishun = BaiduHanZiUtil.word_bishun(doc);
                            wordBaidu.html = html;
                            wordRepository.save(word, Word
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
                                    .setStrokenames(wordBaidu.strokenames)
                                    .setStrokes_count(wordBaidu.strokes_count)
                                    .setBasemean(wordBaidu.basemean)
                                    .setDetailmean(wordBaidu.detailmean)
                                    .setTerms(wordBaidu.terms)
                                    .setRiddles(wordBaidu.riddles)
                                    .setFanyi(wordBaidu.fanyi)
                                    .setBishun(wordBaidu.bishun)
                                    .setDefined(Boolean.TRUE)
                                    .setDefineCreated(System.currentTimeMillis())
                                    .build());
                            logger.info(String.format("✔ baidu defined %x idx=%04d", word.codePointAt(0), newIdx));
                        } else {
                            wordObj.setDefined(Boolean.FALSE);
                            wordObj.setDefineCreated(System.currentTimeMillis());
                            wordRepository.save(word, wordObj);
                            logger.info(String.format("✕ baidu NOT defined %x idx=%04d", word.codePointAt(0), newIdx));
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private boolean isValidStrokeName(String s) {
        boolean valid = false;
        if (StringUtils.isNotEmpty(s)) {
            String[] names = StringUtils.splitByWholeSeparator(s, "/");

            for (int i = 0; i < names.length; i++) {
                String[] definition = HanZi.STROKE_NAMES.get(names[i]);
                if (definition != null) {
                    valid = true;
                    break;
                }
            }
        }
        return valid;
    }


    private void checkStrokeDefinitions() {
        Iterator<String> nameIterator = HanZi.STROKE_NAMES.keySet().iterator();
        while (nameIterator.hasNext()) {
            String name = nameIterator.next();
            if (!foundStrokeNameUsage(name, HanZi.LEVEL1)
                    && !foundStrokeNameUsage(name, HanZi.LEVEL2)
                    && !foundStrokeNameUsage(name, HanZi.LEVEL3)) {
                logger.info(String.format("not found stroke %s in use", name));
            }
        }
    }

    private boolean foundStrokeNameUsage(String name, String[] words) {
        boolean found = false;
        for (int i = 0; i < words.length; i++) {
            Word word = wordRepository.findOne(words[i]);
            List<String> strokeNames = word.getStrokenames();
            if (strokeNames != null) {
                Iterator<String> iterator = strokeNames.iterator();
                while (iterator.hasNext()) {
                    String[] namesArr = StringUtils.splitByWholeSeparator(iterator.next(), "/");
                    for (int j = 0; j < namesArr.length; j++) {
                        if (name.compareToIgnoreCase(namesArr[j]) == 0) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
            }
            if (found) {
                break;
            }
        }
        return found;
    }

    private void checkWordStrokNameDefined(String wordStr) {
        String[] words = StringUtils.splitByWholeSeparator(wordStr, ",");
        for (int i = 0; i < words.length; i++) {
            Word word = wordRepository.findOne(words[i]);
            List<String> strokeNames = word.getStrokenames();
            if (strokeNames != null) {
                Iterator<String> iterator = strokeNames.iterator();
                while (iterator.hasNext()) {
                    String name = iterator.next();
                    String[] nameArr = StringUtils.splitByWholeSeparator(name, "/");
                    for (int j = 0; j < nameArr.length; j++) {
                        if (HanZi.STROKE_NAMES.get(nameArr[j]) == null) {
                            logger.warning(String.format("%s part of %s not defined", nameArr[j], name));
                        }
                    }
                }
            }
        }
    }
}
