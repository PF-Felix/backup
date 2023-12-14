package com.github.gpf.mybatis.sourcecode.entity;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString
public class Student {
    private Integer id;

    private String name;

    private Integer age;

    private final LocalDate birth = LocalDate.now();

    private String sex;

    private Student friend;

    private String[] friends;
}
