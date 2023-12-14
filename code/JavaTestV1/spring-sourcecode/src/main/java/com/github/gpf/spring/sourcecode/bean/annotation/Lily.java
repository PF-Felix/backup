package com.github.gpf.spring.sourcecode.bean.annotation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Lily {

    @Bean
    public Liming newLiming(){
        return new Liming();
    }
}
