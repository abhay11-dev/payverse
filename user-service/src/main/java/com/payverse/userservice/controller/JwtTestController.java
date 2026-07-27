package com.payverse.userservice.controller;

import com.payverse.userservice.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test/jwt")
public class JwtTestController {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtTestController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/generate")
    public Map<String, String> generateToken(
            @RequestParam String username) {

        String token = jwtTokenProvider.generateToken(username);

        return Map.of(
                "token", token
        );
    }

    @GetMapping("/validate")
    public Map<String, Boolean> validateToken(
            @RequestParam String token) {

        boolean valid = jwtTokenProvider.validateToken(token);

        return Map.of(
                "valid", valid
        );
    }

    @GetMapping("/claims")
    public Map<String, Object> extractClaims(
            @RequestParam String token) {

        Claims claims = jwtTokenProvider.extractClaims(token);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("subject", claims.getSubject());
        response.put("issuedAt", claims.getIssuedAt());
        response.put("expiration", claims.getExpiration());

        return response;
    }
}