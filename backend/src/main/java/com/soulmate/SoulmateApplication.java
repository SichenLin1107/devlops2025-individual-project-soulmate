package com.soulmate;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

/**
 * SoulMate 心伴 - AI心理陪伴平台
 * 主启动类
 * 
 * @author SoulMate Team
 * @version 1.0.0
 */
@Slf4j
@SpringBootApplication
@MapperScan("com.soulmate.module.*.mapper")
public class SoulmateApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoulmateApplication.class, args);
        log.info("========================================");
        log.info("  SoulMate 心伴 - AI心理陪伴平台");
        log.info("  启动成功！");
        log.info("========================================");
    }

    @PostConstruct
    public void init() {
        // 设置默认时区为北京时间
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }
}

