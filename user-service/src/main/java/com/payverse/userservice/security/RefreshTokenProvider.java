package com.payverse.userservice.security;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RefreshTokenProvider {

    public String generateRefreshToken() {

        return UUID.randomUUID().toString();
    }
}