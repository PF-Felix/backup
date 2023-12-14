package com.github.gpf.spring.sourcecode.bean.annotation;

import com.github.gpf.spring.sourcecode.bean.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Component
public class Tim {

    @Autowired
    private Company companyAutowired;

    @Resource
    private Company company;

    @PostConstruct
    public void m1(){
        System.out.println("PostConstruct");
    }

    @PreDestroy
    public void m2(){
        System.out.println("PreDestroy");
    }
}
