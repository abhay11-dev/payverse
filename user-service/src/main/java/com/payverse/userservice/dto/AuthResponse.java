package com.payverse.userservice.dto;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String type;


    public AuthResponse(
            String accessToken,
            String refreshToken,
            String type) {

        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.type = type;
    }


    public String getAccessToken() {
        return accessToken;
    }


    public String getRefreshToken() {
        return refreshToken;
    }


    public String getType() {
        return type;
    }
}