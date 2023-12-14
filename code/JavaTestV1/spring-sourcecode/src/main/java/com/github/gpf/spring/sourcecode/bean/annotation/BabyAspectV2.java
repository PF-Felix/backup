package com.github.gpf.spring.sourcecode.bean.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BabyAspectV2 {

    @Pointcut("execution(* com.github.gpf.spring.sourcecode.bean.aop.Baby.testV2(..))")
    public void testV2(){

    }

    @Before("testV2()")
    public void before(){
        System.out.println("beforeV2");
    }

    @After("testV2()")
    public void after(){
        System.out.println("afterV2");
    }

    @AfterReturning("testV2()")
    public void afterReturning(){
        System.out.println("afterReturningV2");
    }

    @Around("testV2()")
    public void around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        System.out.println("around before V2");
        proceedingJoinPoint.proceed();
        System.out.println("around after V2");
    }
}
