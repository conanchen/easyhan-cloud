package org.ditto.easyhan.model;

import lombok.Data;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;

/**
 * @see [https://www.javacodegeeks.com/2017/07/apache-ignite-spring-data.html]
 */
@Data
public class Breed implements Serializable {

    @QuerySqlField(index = true)
    private Long id;

    @QuerySqlField(index = true)
    private String name;
}