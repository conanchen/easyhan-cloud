package org.ditto.easyhan.model.qq;

import com.google.common.base.Strings;
import lombok.Data;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;

@Data
public class UserQQ implements Serializable {
    @QuerySqlField(index = true)
    String userId; //local userId
    // info from QQ
    @QuerySqlField(index = true)
    String openid;//, as PrimiaryKey
    @QuerySqlField
    String client_id;
    @QuerySqlField
    String nickname;//      "nickname": "爱探险的朵拉",
    @QuerySqlField
    String gender;//      "gender": "男",
    @QuerySqlField
    String province;//"province": "广东",
    @QuerySqlField
    String city;//"city": "深圳",
    @QuerySqlField
    String year;//"year": "2007",

    @QuerySqlField
    private long created;
    @QuerySqlField
    private long lastUpdated;

    private UserQQ(String userId, String openid, String client_id, String nickname, String gender, String province, String city, String year, long created, long lastUpdated) {
        this.userId = userId;
        this.openid = openid;
        this.client_id = client_id;
        this.nickname = nickname;
        this.gender = gender;
        this.province = province;
        this.city = city;
        this.year = year;
        this.created = created;
        this.lastUpdated = lastUpdated;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String userId; //local userId
        // info from QQ
        private String openid;//, as PrimiaryKey
        private String client_id;
        private String nickname;//"nickname": "爱探险的朵拉",
        private String gender;//"gender": "男",
        private String province;    //"province": "广东",
        private String city;    //"city": "深圳",
        private String year;    //"year": "2007",
        private long created;
        private long lastUpdated;

        Builder() {
        }

        public UserQQ build() {
            String missing = "";
            if (Strings.isNullOrEmpty(userId)) {
                missing += " userId";
            }
            if (Strings.isNullOrEmpty(openid)) {
                missing += " openid";
            }

            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }
            UserQQ wordObj = new UserQQ(userId, openid, client_id, nickname, gender, province, city,
                    year, created, lastUpdated);
            return wordObj;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setOpenid(String openid) {
            this.openid = openid;
            return this;
        }

        public Builder setClient_id(String client_id) {
            this.client_id = client_id;
            return this;
        }

        public Builder setNickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder setGender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder setProvince(String province) {
            this.province = province;
            return this;
        }

        public Builder setCity(String city) {
            this.city = city;
            return this;
        }

        public Builder setYear(String year) {
            this.year = year;
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
