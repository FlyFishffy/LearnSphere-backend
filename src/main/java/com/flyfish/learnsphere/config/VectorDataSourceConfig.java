package com.flyfish.learnsphere.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * PostgreSQL vector datasource configuration for RAG
 * @Author: FlyFish
 * @CreateTime: 2026/02/27
 */
@Configuration
@Lazy
public class VectorDataSourceConfig {

    @Value("${spring.vector-datasource.url}")
    private String url;

    @Value("${spring.vector-datasource.username}")
    private String username;

    @Value("${spring.vector-datasource.password:}")
    private String password;

    @Value("${spring.vector-datasource.driver-class-name}")
    private String driverClassName;

    @Bean(name = "vectorDataSource")
    @Lazy
    public DataSource vectorDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean(name = "vectorJdbcTemplate")
    @Lazy
    public JdbcTemplate vectorJdbcTemplate(@Qualifier("vectorDataSource") DataSource vectorDataSource) {
        return new JdbcTemplate(vectorDataSource);
    }
}
