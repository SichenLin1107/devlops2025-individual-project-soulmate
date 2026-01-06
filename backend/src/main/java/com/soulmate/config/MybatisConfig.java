package com.soulmate.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis 配置类
 */
@Configuration
public class MybatisConfig {

    /**
     * 配置 SqlSessionFactory
     * 支持驼峰命名转换（数据库下划线 -> Java驼峰）
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        
        // MyBatis 配置
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        // 开启驼峰命名转换
        configuration.setMapUnderscoreToCamelCase(true);
        // 开启日志（开发环境）
        configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
        
        factoryBean.setConfiguration(configuration);
        
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            factoryBean.setMapperLocations(resolver.getResources("classpath:mapper/**/*.xml"));
        } catch (Exception e) {
            // 如果没有 XML 文件，忽略此配置
        }
        
        return factoryBean.getObject();
    }
}

