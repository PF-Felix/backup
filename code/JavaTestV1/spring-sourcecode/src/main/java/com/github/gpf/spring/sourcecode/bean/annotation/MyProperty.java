package com.github.gpf.spring.sourcecode.bean.annotation;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@PropertySource("/com/github/gpf/spring/sourcecode/p")
public class MyProperty {

    @Value("${x}")
    private int x;
}
