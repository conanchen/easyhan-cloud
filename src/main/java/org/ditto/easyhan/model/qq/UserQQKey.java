package org.ditto.easyhan.model.qq;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserQQKey implements Serializable {
    private String openid;

    public UserQQKey() {
    }

    public UserQQKey(String openid) {
        this.openid = openid;
    }
}
