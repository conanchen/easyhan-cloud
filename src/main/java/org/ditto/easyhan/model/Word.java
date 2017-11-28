package org.ditto.easyhan.model;

import com.google.common.base.Strings;
import lombok.Data;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.ditto.easyhan.grpc.WordBaidu;
import org.easyhan.common.grpc.HanziLevel;

import java.io.Serializable;
import java.util.List;

/**
 * @see [https://www.javacodegeeks.com/2017/07/apache-ignite-spring-data.html]
 */
@Data
public class Word implements Serializable {

    @QuerySqlField(index = true)
    private String word;
    @QuerySqlField(index = true)
    private HanziLevel level;
    @QuerySqlField(index = true)
    private int levelIdx;
    @QuerySqlField
    private long created;
    @QuerySqlField(index = true)
    private long lastUpdated;
    @QuerySqlField(index = true)
    private int visitCount;

    @QuerySqlField
    private List<Pinyin> pinyins;
    @QuerySqlField
    private String radical;
    @QuerySqlField
    private String wuxing;
    @QuerySqlField
    private String traditional;
    @QuerySqlField
    private String wubi;
    @QuerySqlField
    private List<String> strokes;
    @QuerySqlField
    private int strokes_count;
    @QuerySqlField
    private String basemean;
    @QuerySqlField
    private String detailmean;
    @QuerySqlField
    private List<String> terms;
    @QuerySqlField
    private List<String> riddles;
    @QuerySqlField
    private String fanyi;
    @QuerySqlField
    private String bishun;

    public Word() {
    }

    private Word(String word, HanziLevel level, int levelIdx, long created, long lastUpdated, int visitCount, List<Pinyin> pinyins, String radical, String wuxing, String traditional, String wubi, List<String> strokes, Integer strokes_count, String basemean, String detailmean, List<String> terms, List<String> riddles, String fanyi, String bishun) {
        this.word = word;
        this.level = level;
        this.levelIdx = levelIdx;
        this.created = created;
        this.lastUpdated = lastUpdated;
        this.visitCount = visitCount;
        this.pinyins = pinyins;
        this.radical = radical;
        this.wuxing = wuxing;
        this.traditional = traditional;
        this.wubi = wubi;
        this.strokes = strokes;
        this.strokes_count = strokes_count;
        this.basemean = basemean;
        this.detailmean = detailmean;
        this.terms = terms;
        this.riddles = riddles;
        this.fanyi = fanyi;
        this.bishun = bishun;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String word;
        private HanziLevel level;
        private int levelIdx;
        private long created;
        private long lastUpdated;
        private int visitCount;

        private List<Pinyin> pinyins;
        private String radical;
        private String wuxing;
        private String traditional;
        private String wubi;
        private List<String> strokes;
        private int strokes_count;
        private String basemean;
        private String detailmean;
        private List<String> terms;
        private List<String> riddles;
        private String fanyi;
        private String bishun;

        Builder() {
        }

        public Word build() {
            String missing = "";
            if (Strings.isNullOrEmpty(word)) {
                missing += " word";
            }
            if (level == null) {
                missing += " level";
            }

            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }
            Word wordObj = new Word(word, level, levelIdx, created, lastUpdated, visitCount,
                    pinyins, radical, wuxing, traditional, wubi,
                    strokes, strokes_count, basemean, detailmean, terms, riddles, fanyi, bishun);
            return wordObj;
        }

        public Builder setWord(String word) {
            this.word = word;
            return this;
        }

        public Builder setLevel(HanziLevel level) {
            this.level = level;
            return this;
        }

        public Builder setLevelIdx(int levelIdx) {
            this.levelIdx = levelIdx;
            return this;
        }

        public Builder setCreated(long created) {
            this.created = created;
            return this;
        }

        public Builder setLastUpdated(long lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public Builder setVisitCount(int visitCount) {
            this.visitCount = visitCount;
            return this;
        }

        public Builder setPinyins(List<Pinyin> pinyins) {
            this.pinyins = pinyins;
            return this;
        }

        public Builder setRadical(String radical) {
            this.radical = radical;
            return this;
        }

        public Builder setWuxing(String wuxing) {
            this.wuxing = wuxing;
            return this;
        }

        public Builder setTraditional(String traditional) {
            this.traditional = traditional;
            return this;
        }

        public Builder setWubi(String wubi) {
            this.wubi = wubi;
            return this;
        }

        public Builder setStrokes(List<String> strokes) {
            this.strokes = strokes;
            return this;
        }

        public Builder setStrokes_count(int strokes_count) {
            this.strokes_count = strokes_count;
            return this;
        }

        public Builder setBasemean(String basemean) {
            this.basemean = basemean;
            return this;
        }

        public Builder setDetailmean(String detailmean) {
            this.detailmean = detailmean;
            return this;
        }

        public Builder setTerms(List<String> terms) {
            this.terms = terms;
            return this;
        }

        public Builder setRiddles(List<String> riddles) {
            this.riddles = riddles;
            return this;
        }

        public Builder setFanyi(String fanyi) {
            this.fanyi = fanyi;
            return this;
        }

        public Builder setBishun(String bishun) {
            this.bishun = bishun;
            return this;
        }
    }
}