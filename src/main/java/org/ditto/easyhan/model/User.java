package org.ditto.easyhan.model;

import com.google.common.base.Strings;
import lombok.Data;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;

@Data
public class User implements Serializable {
    @QuerySqlField(index = true)
    UserKey.IDIssuer idIssuer;
    @QuerySqlField(index = true)
    String id;
    @QuerySqlField
    String nickname;
    @QuerySqlField
    private long created;
    @QuerySqlField
    private long lastUpdated;

    public User(UserKey.IDIssuer idIssuer, String id, String nickname, long created, long lastUpdated) {
        this.idIssuer = idIssuer;
        this.id = id;
        this.nickname = nickname;
        this.created = created;
        this.lastUpdated = lastUpdated;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UserKey.IDIssuer idIssuer;
        private String id;
        private String nickname;
        private long created;
        private long lastUpdated;

        Builder() {
        }

        public User build() {
            String missing = "";
            if (idIssuer == null) {
                missing += " idType";
            }
            if (Strings.isNullOrEmpty(id)) {
                missing += " id";
            }

            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }

            User user = new User(idIssuer, id, nickname, created, lastUpdated);
            return user;
        }

        public Builder setIdIssuer(UserKey.IDIssuer idIssuer) {
            this.idIssuer = idIssuer;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setNickname(String nickname) {
            this.nickname = nickname;
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
