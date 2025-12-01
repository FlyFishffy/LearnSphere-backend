package com.flyfish.learnsphere.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/20
 */
@Configuration
public class SecurityConfig {

    /**
     * 配置密码编辑器
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(12);
    }
}
