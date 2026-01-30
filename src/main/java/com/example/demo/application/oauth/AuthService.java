package com.example.demo.application.oauth;

import com.example.demo.application.dto.TokenResponse;
import com.example.demo.domain.RefreshToken;
import com.example.demo.domain.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponse issueTokens(Long userId) {
        TokenResponse token = tokenProvider.generateToken(userId);

        RefreshToken refreshToken = refreshTokenRepository.findByUserId((userId))
            .orElseGet(() -> new RefreshToken(userId, token.refreshToken()));

        refreshToken.rotate(token.refreshToken());
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
