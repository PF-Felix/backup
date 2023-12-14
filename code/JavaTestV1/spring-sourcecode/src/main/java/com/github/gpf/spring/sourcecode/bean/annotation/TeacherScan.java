package com.github.gpf.spring.sourcecode.bean.annotation;

import com.github.gpf.spring.sourcecode.exBean.Teacher;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = Teacher.class)
public class TeacherScan {
}
