package com.example.demo.infrastructure.controller.dto;

import com.example.demo.application.dto.TokenResponse;

public record AuthTokenWebResponse(
    String accessToken,
    String refreshToken
) {
    public static AuthTokenWebResponse from(TokenResponse tokenResponse) {
        return new AuthTokenWebResponse(tokenResponse.accessToken(), tokenResponse.refreshToken());
    }
}
