package com.github.gpf.spring.sourcecode.bean.annotation;

import com.github.gpf.spring.sourcecode.exBean.Man;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(Man.class)
public class ManImport {
}
