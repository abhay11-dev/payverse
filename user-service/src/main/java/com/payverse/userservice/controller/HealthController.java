package com.payverse.userservice.controller;

import com.payverse.userservice.dto.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public BaseResponse<Map<String, String>> health() {

        return BaseResponse.success(
                Map.of(
                        "service", "User Service",
                        "status", "UP"
                ),
                "User Service is running"
        );
    }
}