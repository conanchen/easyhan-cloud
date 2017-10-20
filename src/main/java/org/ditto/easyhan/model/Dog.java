package org.ditto.easyhan.model;

import lombok.Data;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;
import java.sql.Date;

@Data
public class Dog implements Serializable {

    @QuerySqlField(index = true)
    private Long id;
    @QuerySqlField(index = true)
    private String name;
    @QuerySqlField(index = true)
    private Long breedid;
    @QuerySqlField(index = true)
    private Date birthdate;

}