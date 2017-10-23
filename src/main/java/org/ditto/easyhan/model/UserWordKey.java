package org.ditto.easyhan.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserWordKey implements Serializable {
    private String userId;
    private String word;

    public UserWordKey() {
    }

    public UserWordKey(String userId, String word) {
        this.userId = userId;
        this.word = word;
    }
}
