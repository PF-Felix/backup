package com.github.gpf.spring.sourcecode.bean.aop;

import org.aspectj.lang.ProceedingJoinPoint;

public class BabyAspect {
    public void before(){
        System.out.println("before");
    }

    public void after(){
        System.out.println("after");
    }

    public void afterReturning(){
        System.out.println("afterReturning");
    }

    public void around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        System.out.println("around before");
        proceedingJoinPoint.proceed();
        System.out.println("around after");
    }
}
