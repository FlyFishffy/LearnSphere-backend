package com.flyfish.learnsphere.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/20
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI learnSphereOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Learn Sphere API")
                        .description("接口文档")
                        .version("1.0.0"));
    }
}
