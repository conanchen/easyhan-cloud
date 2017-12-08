package org.ditto.easyhan.data;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.ditto.easyhan.model.Word;
import org.ditto.easyhan.repository.WordRepository;
import org.easyhan.common.grpc.HanziLevel;
import org.easyhan.word.HanZi;
import org.easyhan.word.grpc.WordProto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 服务启动执行
 */
@Component
@Slf4j
public class WordDataImportRunner implements CommandLineRunner {
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
    }

    private int saveWords(String hanziText, HanziLevel level, int startIdx) {
        String[] words = hanziText.split(",");
        for (int i = 0; i < words.length; i++) {
            int newIdx = i + startIdx;
            String word = words[i];
            upsertWordWithLevelInfo(word, level, newIdx);
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
            log.info(String.format("new %04d,%s", newIdx, word));
        } else {
            if (level.compareTo(wordObj.getLevel()) != 0
                    || newIdx != wordObj.getLevelIdx()
                    ) {
                log.info(String.format("upd %04d,%s", newIdx, word));
                wordObj.setLevel(level);
                wordObj.setLevelIdx(newIdx);
                wordObj.setLastUpdated(System.currentTimeMillis());
                wordRepository.save(word, wordObj);
            }
        }
    }
}
