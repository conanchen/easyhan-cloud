package org.ditto.easyhan.model;

import com.google.common.base.Strings;
import lombok.Data;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;

/**
 * @see [https://www.javacodegeeks.com/2017/07/apache-ignite-spring-data.html]
 */
@Data
public class UserWord implements Serializable {

    @QuerySqlField(index = true)
    private String userId;
    @QuerySqlField(index = true)
    private String word;
    @QuerySqlField(index = true)
    private int memIdx;
    @QuerySqlField
    private long created;
    @QuerySqlField(index = true)
    private long lastUpdated;

    public UserWord() {
    }

    private UserWord(String userId, String word, int memIdx, long created, long lastUpdated) {
        this.userId = userId;
        this.word = word;
        this.memIdx = memIdx;
        this.created = created;
        this.lastUpdated = lastUpdated;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String userId;
        private String word;
        private int memIdx;
        private long created;
        private long lastUpdated;

        Builder() {
        }

        public UserWord build() {
            String missing = "";
            if (Strings.isNullOrEmpty(userId)) {
                missing += " id";
            }

            if (Strings.isNullOrEmpty(word)) {
                missing += " word";
            }

            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }
            UserWord wordObj = new UserWord(userId, word, memIdx, created, lastUpdated);
            return wordObj;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setWord(String word) {
            this.word = word;
            return this;
        }

        public Builder setMemIdx(int memIdx) {
            this.memIdx = memIdx;
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
    }
}