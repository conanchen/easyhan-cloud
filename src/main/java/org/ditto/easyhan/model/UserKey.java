package org.ditto.easyhan.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserKey implements Serializable {
    private IDIssuer idIssuer;
    private String id;

    public enum IDIssuer {
        QQ,WECHAT,WEIBO,LOCAL
    }
    public UserKey() {
    }

    public UserKey(IDIssuer idIssuer, String id) {
        this.idIssuer = idIssuer;
        this.id = id;
    }
}
