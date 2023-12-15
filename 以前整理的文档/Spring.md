# SpringBoot

## jar包启动的原理

无论是 maven 还是 gradle 编译生成的 springboot 可执行的 jar 都是如下的目录结构：

- BOOT-INF 目录中是我们的应用程序，包含字节码文件（classes）和第三方依赖包（lib）
- META-INF 会有一个清单文件 MANIFEST.MF
- org 是 springboot-boot-loader 模块的字节码文件

清单文件 MANIFEST.MF 的内容如下：

- Main-Class 定义了可执行的 jar 包的启动类 JarLauncher，起点就是 JarLauncher 的 main 方法
- JarLauncher 会构造一个合适的类加载器来加载应用程序的字节码，应用程序的入口在 Start-Class 指定的类的 main 方法，spring-boot-loader 模块会通过反射调用它

为何使用这种方式打包？因为应用类加载器无法加载嵌套的 jar 中的字节码，因此就有了 springboot-boot-loader 这个模块通过使用自定义类加载器加载嵌套的 jar

PS：也可以在 IDE 直接通过启动类的 main 方法启动，本质都是调用启动类的 main 方法

# Spring面试题

## 说说你对Spring的理解

==发展历程==
Spring 最初是单纯基于 xml 的，后来支持了注解简化了开发
再到后来 springboot 的出现更加简化了开发和部署（内嵌了Tomcat）

==组成部分==

> Spring Core：核心类库，提供 IOC 服务
> Spring AOP：AOP 服务
> Spring DAO：对 JDBC 的抽象，简化了数据访问
> Spring ORM：对现有的 ORM 框架的支持
> Spring MVC：提供面向 Web 应用的 Model-View-Controller 实现

==优势==

> 轻量
> 两个重要特性：IOC 与 AOP
> 提供了精心设计的 WEB MVC 框架
> 提供了事务管理
> 学习上手简单

## 什么是IOC/DI

控制反转，就是把原先我们代码里面需要实现的对象创建、依赖的代码，反转给容器来帮忙实现

## 什么是AOP

AOP是对现有功能的增强，可用于事务管理、记录日志、缓存

实现原理是动态代理，接口类型默认使用 JDK，非接口类型使用 CGLIB

==基本术语==
Aspect：切面
advice：通知
连接点：join point
切点：point cut

继续往下扩展说，具体点在哪里实现的动态代理？参考《容器启动过程-initializeBean》

通知类型：

- Before：方法之前执行
- After Returng：方法正常执行后执行
- After Throwing：方法抛出异常时执行
- After（finally）：方法执行之后执行，无论方法正常退出还是异常返回都会执行
- Around：方法之前和之后执行

## Spring中的设计模式

单例模式：Bean默认都是单例的

原型模式：拷贝属性创建新的对象（本质是深度克隆生成新对象）

责任链模式：使用AOP进行通知调用的时候，使用的拦截器链

工厂模式：IOC容器是工厂模式；FactoryBean也是工厂模式；factory-method创建对象也是工厂模式

代理模式：使用 JDK 或 cglib 生成动态代理

模板方法模式：Spring中应用了大量的模板方法模式；容器启动的时候 refresh 方法就是模板方法模式，有几个步骤方法需要子类实现的 postProcessBeanFactory、onRefresh

观察者模式：广播器、事件、监听器

策略模式：GroovyBeanDefinitionReader、XmlBeanDefinitionReader

适配器模式：SpringMVC 中的 HandlerAdapter，统一不同的 handler 的调用

## 谈谈对循环依赖的理解

## Spring的事件机制

基于《观察者模式》

典型应用就是 springboot 启动过程中发布了很多事件，监听器监听到事件之后做一些操作，不同的监听器支持的事件类型不同
很好地实现了模块之间的解耦

## 介绍下Spring的事务处理

==事务的四个特性==参考《Mysql》

==实现的两种方式==XML 与注解

==事务的传播行为==

用于事务嵌套

![image-20230329014415125](C:\backup\assets\image-20230329014415125.png)

支持外层事务的有：

- required（支持当前事务，没有就创建一个）
- supports（支持当前事务，没有就不用事务了，以非事务方式运行）
- mandatory（支持当前事务，没有就抛出异常）

不支持外层事务：

- required new（每次都创建一个新事务，挂起当前事务）
- not supported（不用事务，以非事务方式运行，挂起当前事务）
- never（不用事务，以非事务方式运行，有的话抛出异常）

nested：如果存在事务，创建保存点，不存在就创建新事务

==事务的隔离级别==
一共五个，其中四个同数据库，另一个是默认的隔离级别使用数据库本身使用的隔离级别；一般使用的就是默认

==事务的实现==
通过 AOP 实现，继续往下说，参考《什么是AOP》

## 单例的Bean是线程安全的吗

不是，有共享变量的话可能存在线程安全问题

## Spring支持的几种Bean的作用域

singleton：单例，默认的作用域
prototype：原型模式，需要的时候创建一个新实例
request：每次 http 请求都会创建一个bean
session：一次会话创建一个实例
global-session：全局session，应用级别的

## 有哪些依赖注入方式

构造器注入、Setter方法注入

哪种依赖注入方式你建议使用，构造器注入，还是 Setter方法注入TODO

## 如何加密属性文件中的账号密码

可以实现 EnvironmentPostProcessor 后置处理器接口做解密处理

## SpringMVC和Struts2的区别有哪些

SpringMVC入口是一个servlet即DispatchServlet，而Struts2入口是一个filter过滤器

SpringMVC可以使用注解基于方法开发（一个url对应一个方法），默认是单例，可以设置为多例
Struts2是基于类开发，只能设计为多例

## @RequestParam@PathVariable对比

`@RequestParam`对应的 URL 是这样的：http://host:port/path?username=gpf
`@PathVariable`对应的 URL 是这样的：http://host:port/path/gpf

```java
@RequestMapping(value = "/path/{draftId}", method = RequestMethod.GET)
public DraftDetail get(@PathVariable("draftId") long draftId) {
    return draftService.getDraft(draftId);
}

@RequestMapping(value = "/path/{draftId}/dict-data/find", method = RequestMethod.GET)
public DraftDictData find(@PathVariable("draftId") long draftId, @RequestParam("code") String code) {
    return draftService.findDictData(draftId, code);
}
```

## 常见的注解有哪些

可以通过 import 将面试官引导到《SpringBoot的自动装配》

## 介绍下Bean的生命周期

Bean的整个生命周期是依赖于容器的，容器启动之后Bean都实例化了，容器关闭Bean也会销毁
Bean的实例化其实就是《容器的启动过程》

# SpringBoot面试题

## 工作原理

1、首先它是基于 IOC 与 AOP 的，谈一下《SpringBoot启动流程》
2、然后它实现了《自动装配》，并且 web 应用内嵌了 tomcat，极大的简化方便了开发和部署
要说一下上面两步是怎么关联起来的

## 优点

拥有 Spring 的一切优点

==独立运行==
SpringBoot 项目内嵌了 web 容器，不需要打成 war 包部署到容器中，而是生成一个可以独立运行的、包含所有依赖包的 jar

==简化配置==

==自动装配==

## 如何在启动时候运行一些特定的代码

让一个 Bean 类实现 ApplicationRunner 接口，在 run 方法中可以随意编写代码

## 如何实现定时任务

方法1：使用`@Scheduled`注解

方法2：使用第三方框架`Quartz`

## bootstrap.yml的意义

默认支持的属性文件有下面4种：

- application.properties
- application.xml
- application.yml
- application.yaml

bootstrap.properties、bootstrap.yml 需要在 SpringCloud 环境下才支持，作用是在 SpringBoot 项目启动之前启动一个父容器，该父容器可以在 SpringBoot 容器启动之前完成一些加载初始化的操作，比如加载配置中心中的信息