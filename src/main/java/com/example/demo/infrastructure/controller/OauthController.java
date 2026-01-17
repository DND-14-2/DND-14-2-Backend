package com.example.demo.infrastructure.controller;

import com.example.demo.application.oauth.AuthService;
import com.example.demo.application.oauth.OauthService;
import com.example.demo.application.dto.TokenResponse;
import com.example.demo.domain.User;
import com.example.demo.infrastructure.interceptor.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OauthController {

    private final OauthService oauthService;
    private final AuthService authService;

    @PostMapping("/oauth/google")
    public ResponseEntity<TokenResponse> googleLogin(@RequestParam("code") String authorizationCode) {
        User userInfo = oauthService.getUserInfo(authorizationCode);
        TokenResponse tokenResponse = authService.issueTokens(userInfo);

        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@UserId Long userId) {
        authService.logout(userId);

        return ResponseEntity.noContent().build();
    }
}
