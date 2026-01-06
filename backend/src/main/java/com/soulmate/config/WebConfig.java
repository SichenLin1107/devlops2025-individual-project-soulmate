package com.soulmate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Web 配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private static final String UPLOAD_RESOURCE_HANDLER = "/uploads/**";
    private static final String UPLOAD_RESOURCE_LOCATION = "file:./uploads/";
    
    /**
     * 注意：CORS 配置在 SecurityConfig 中统一管理，此处不再重复配置
     */
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(UPLOAD_RESOURCE_HANDLER)
                .addResourceLocations(UPLOAD_RESOURCE_LOCATION);
    }

    /**
     * 配置 Jackson ObjectMapper，统一 LocalDateTime 序列化格式
     * 使用 yyyy-MM-dd HH:mm:ss 格式
     */
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, 
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return builder
                .modules(javaTimeModule)
                .timeZone(TimeZone.getTimeZone("Asia/Shanghai"))
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}
