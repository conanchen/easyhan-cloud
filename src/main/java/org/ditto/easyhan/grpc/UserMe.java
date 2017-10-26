package org.ditto.easyhan.grpc;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserMe implements Serializable{
    public String name;

    public UserMe(String name) {
        this.name = name;
    }
}
