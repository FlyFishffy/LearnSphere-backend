package com.flyfish.learnsphere;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.flyfish.learnsphere.mapper")
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class LearnSphereApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearnSphereApplication.class, args);
    }

}
