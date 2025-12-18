package com.tencent.supersonic.demo.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * SuperSonic Demo 应用启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.tencent.supersonic.demo")
@MapperScan("com.tencent.supersonic.demo.semantic.repository")
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
