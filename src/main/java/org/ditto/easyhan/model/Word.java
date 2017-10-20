package org.ditto.easyhan.model;

import com.google.common.base.Strings;
import lombok.Data;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.easyhan.common.grpc.HanziLevel;

import java.io.Serializable;

/**
 * @see [https://www.javacodegeeks.com/2017/07/apache-ignite-spring-data.html]
 */
@Data
public class Word implements Serializable {

    @QuerySqlField(index = true)
    private String word;
    @QuerySqlField(index = true)
    private int idx;
    @QuerySqlField
    private String pinyin;
    @QuerySqlField(index = true)
    private HanziLevel level;
    @QuerySqlField
    private long created;
    @QuerySqlField(index = true)
    private long lastUpdated;
    @QuerySqlField(index = true)
    private int visitCount;


    public Word() {
    }


    private Word(String word, int idx, String pinyin, HanziLevel level, long created, long lastUpdated, int visitCount) {
        this.word = word;
        this.idx = idx;
        this.pinyin = pinyin;
        this.level = level;
        this.created = created;
        this.lastUpdated = lastUpdated;
        this.visitCount = visitCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String word;
        private int idx;
        private String pinyin;
        private HanziLevel level;
        private long created;
        private long lastUpdated;
        private int visitCount;

        Builder() {
        }

        public Word build() {
            String missing = "";
            if (Strings.isNullOrEmpty(word)) {
                missing += " word";
            }
            if (level ==null) {
                missing += " level";
            }

            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }
            Word wordObj = new Word(  word, idx, pinyin, level,   created,   lastUpdated,   visitCount);
            return wordObj;
        }

        public Builder setWord(String word) {
            this.word = word;
            return this;
        }

        public Builder setIdx(int idx) {
            this.idx = idx;
            return this;
        }

        public Builder setPinyin(String pinyin) {
            this.pinyin = pinyin;
            return this;
        }

        public Builder setLevel(HanziLevel level) {
            this.level = level;
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
    }
}