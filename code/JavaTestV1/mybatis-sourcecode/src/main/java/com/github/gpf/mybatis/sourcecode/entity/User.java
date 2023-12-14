package com.github.gpf.mybatis.sourcecode.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * (User)实体类
 *
 * @author makejava
 * @since 2023-03-11 21:21:32
 */

@Data
@Builder
public class User implements Serializable {
    private static final long serialVersionUID = 760372771301086962L;

    private Integer id;

    private String name;

    private Integer age;
}