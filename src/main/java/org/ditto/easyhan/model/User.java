package org.ditto.easyhan.model;

import com.google.common.base.Strings;
import lombok.Data;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;

@Data
public class User implements Serializable {
    @QuerySqlField(index = true)
    String userId;
    @QuerySqlField
    String nickname;
    @QuerySqlField
    String accessToken;
    @QuerySqlField
    long expiresIn;
    @QuerySqlField
    private long created;
    @QuerySqlField
    private long lastUpdated;


    public User(String userId, String nickname, String accessToken, long expiresIn, long created, long lastUpdated) {
        this.userId = userId;
        this.nickname = nickname;
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.created = created;
        this.lastUpdated = lastUpdated;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String userId;
        private String nickname;
        private String accessToken;
        private long expiresIn;
        private long created;
        private long lastUpdated;

        Builder() {
        }

        public User build() {
            String missing = "";
            if (Strings.isNullOrEmpty(userId)) {
                missing += " userId";
            }

            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }

            User user = new User(userId, nickname, accessToken, expiresIn, created, lastUpdated);
            return user;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setNickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder setAccessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder setExpiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
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
