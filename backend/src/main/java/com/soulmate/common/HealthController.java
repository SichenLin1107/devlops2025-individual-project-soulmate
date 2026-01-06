package com.soulmate.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/v1/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "SoulMate API");
        data.put("version", "1.0.0");
        data.put("time", LocalDateTime.now().toString());
        return ApiResponse.success(data);
    }
}

