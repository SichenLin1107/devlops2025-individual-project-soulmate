package com.soulmate.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置类
 * 用于调用外部 API，如 RAG 服务
 */
@Slf4j
@Configuration
public class RestTemplateConfig {
    
    @Value("${rag.service.connect-timeout:30000}")
    private int connectTimeout;
    
    @Value("${rag.service.read-timeout:300000}")
    private int readTimeout;  // 5分钟超时（向量化可能需要较长时间）
    
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(clientHttpRequestFactory());
        log.info("RestTemplate配置完成: connectTimeout={}ms, readTimeout={}ms", connectTimeout, readTimeout);
        return restTemplate;
    }
    
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);  // 连接超时30秒
        factory.setReadTimeout(readTimeout);        // 读取超时120秒（LLM响应可能较慢）
        return factory;
    }
}
