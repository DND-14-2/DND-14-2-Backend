package com.example.demo.application.oauth;

import com.example.demo.application.dto.TokenResponse;
import com.example.demo.domain.RefreshToken;
import com.example.demo.domain.RefreshTokenRepository;
import com.example.demo.domain.User;
import com.example.demo.infrastructure.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponse issueTokens(User user) {
        TokenResponse token = tokenProvider.generateToken(user.getId());

        RefreshToken refreshToken = refreshTokenRepository.findByUserId((user.getId()))
            .orElseGet(() -> new RefreshToken(user.getId(), token.refreshToken()));

        refreshToken.rotate(token.refreshToken());
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public TokenResponse reissueToken(String refreshToken) {
        Long userId = tokenProvider.validateToken(refreshToken);
        RefreshToken findRefreshToken = refreshTokenRepository.findByUserId(userId)
            .orElseThrow(() -> new UnauthorizedException("인증되지 않은 사용자입니다."));

        if (!findRefreshToken.isSameToken(refreshToken)) {
            throw new UnauthorizedException("인증되지 않은 사용자입니다.");
        }

        TokenResponse tokenResponse = tokenProvider.generateToken(userId);
        findRefreshToken.rotate(tokenResponse.refreshToken());
        refreshTokenRepository.save(findRefreshToken);

        return tokenResponse;
    }
}
