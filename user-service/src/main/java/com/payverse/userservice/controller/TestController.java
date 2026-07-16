package com.payverse.userservice.controller;

import com.payverse.userservice.dto.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import com.payverse.userservice.dto.CreateUserRequest;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// This class handles HTTP requests, and whatever I return should be converted into JSON.
@RestController

public class TestController {

    @GetMapping("/test")
    public BaseResponse<Map<String, String>> test() {

        return BaseResponse.success(
                Map.of(
                        "service", "User Service",
                        "status", "Running"
                ),
                "Service is healthy"
        );
    }


    @GetMapping("/error")
    public BaseResponse<String> testError() {
        throw new RuntimeException("Testing Global Exception Handler");
    }


    @PostMapping("/test")
public BaseResponse<String> test(@Valid @RequestBody CreateUserRequest request) {
    return BaseResponse.success("OK", "Success");
}
} 