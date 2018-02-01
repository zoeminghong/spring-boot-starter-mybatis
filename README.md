# Spring-Boot-Starter-Mybatis

基于Spring Boot和Mybatis，添加分页相关功能，免配置即可用

## 依赖

spring boot:1.5.6

mybatis-spring-boot-starter:1.3.1

cluster-common:1.2.2-RC

## 快速开始

1. 添加依赖

```xml
<dependency>
    <groupId>com.zerostech</groupId>
    <artifactId>spring-boot-starter-mybatis</artifactId>
    <version>{lastest version}</version>
</dependency>
```

2. JDBC配置

```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:test
    schema: classpath:db/schema.sql
    data: classpath:db/data.sql    
```

3. MyBatis配置

```yaml
mybatis:
    type-aliases-package: com.zeros.demo.model
    type-handlers-package: com.zerso.demo.typehandler
    configuration:
        map-underscore-to-camel-case: true
        default-fetch-size: 100
        default-statement-timeout: 30
```

## 演示

项目目录`example/mybatis-demo`

## 参考

[Mybatis](http://www.mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)

[Spring Boot Reference](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)