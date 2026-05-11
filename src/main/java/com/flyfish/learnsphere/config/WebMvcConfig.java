package com.flyfish.learnsphere.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for serving uploaded files (videos, etc.)
 * @Author: FlyFish
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.video-path:./uploads/videos/}")
    private String videoUploadPath;

    @Value("${file.upload.image-path:./uploads/images/}")
    private String imageUploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded videos at /uploads/videos/**
        registry.addResourceHandler("/uploads/videos/**")
                .addResourceLocations("file:" + videoUploadPath);

        // Serve uploaded images at /uploads/images/**
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations("file:" + imageUploadPath);
    }
}
