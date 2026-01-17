package com.example.demo.infrastructure.oauth.kakao;

import com.example.demo.application.IdTokenVerifier;
import com.example.demo.application.dto.OauthUserInfo;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;

import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Duration;
import java.util.Date;
import java.util.List;

import static com.example.demo.common.config.RestClientConfig.KAKAO_ISSUER;

@RequiredArgsConstructor
@Component
public class KakaoIdTokenVerifier implements IdTokenVerifier {

    private static final URL KAKAO_JWKS_URL = toUrl("https://kauth.kakao.com/.well-known/jwks.json");

    private final JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(KAKAO_JWKS_URL);
    private final KakaoOauthProperties kakaoOauthProperties; // clientId 필요 (aud 검증)


    @Override
    public OauthUserInfo verify(String idToken) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(idToken);
            validateSignature(signedJwt);
            JWTClaimsSet claims = validateClaims(signedJwt);

            String sub = claims.getSubject();
            String email = claims.getStringClaim("email");
            String picture = claims.getStringClaim("picture");

            return new OauthUserInfo(sub, email, picture);
        } catch (Exception e) {
            throw new IllegalArgumentException("id_token 처리 중 알 수 없는 오류가 발생했습니다.", e);
        }
    }

    private void validateSignature(SignedJWT jwt) throws JOSEException {
        JWSAlgorithm alg = jwt.getHeader().getAlgorithm();
        if (!JWSAlgorithm.RS256.equals(alg)) {
            throw new IllegalArgumentException("지원하지 않는 id_token 서명 알고리즘입니다: " + alg);
        }

        String keyId = jwt.getHeader().getKeyID();
        if (keyId == null || keyId.isBlank()) {
            throw new IllegalArgumentException("id_token 헤더에 key id가 없습니다.");
        }

        JWKSelector selector = new JWKSelector(
                new JWKMatcher.Builder()
                        .keyType(KeyType.RSA)
                        .keyID(keyId)
                        .build()
        );
        List<JWK> jwks = jwkSource.get(selector, null);
        if (jwks.isEmpty()) {
            throw new IllegalArgumentException("해당 kid에 매칭되는 공개키를 찾을 수 없습니다. keyId=" + keyId);
        }

        RSAKey rsaKey = (RSAKey) jwks.get(0);
        JWSVerifier verifier = new RSASSAVerifier(rsaKey.toRSAPublicKey());

        if (!jwt.verify(verifier)) {
            throw new IllegalArgumentException("id_token 서명 검증에 실패했습니다.");
        }
    }

    private JWTClaimsSet validateClaims(SignedJWT jwt) throws ParseException {
        JWTClaimsSet claims = jwt.getJWTClaimsSet();

        // issuer (발급자) 검증
        String issuer = claims.getIssuer();
        if (!KAKAO_ISSUER.equals(issuer)) {
            throw new IllegalArgumentException("id_token 발급자가 올바르지 않습니다: " + issuer);
        }

        // 우리 앱 client_id 검증
        String clientId = kakaoOauthProperties.clientId();
        List<String> aud = claims.getAudience();
        if (aud == null || !aud.contains(clientId)) {
            throw new IllegalArgumentException("id_token 대상(aud)이 우리 앱(client_id)과 일치하지 않습니다: " + aud);
        }

        // 토큰 유효 시간 검증
        Date now = new Date();
        long skewMillis = Duration.ofMinutes(1).toMillis();
        Date exp = claims.getExpirationTime();
        if (exp == null || exp.getTime() + skewMillis < now.getTime()) {
            throw new IllegalArgumentException("id_token이 만료되었습니다.");
        }
        Date nbf = claims.getNotBeforeTime();
        if (nbf != null && nbf.getTime() - skewMillis > now.getTime()) {
            throw new IllegalArgumentException("id_token이 아직 유효 시간이 아닙니다(nbf).");
        }
        return claims;
    }

    private static URL toUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("JWKS URL 설정이 올바르지 않습니다: " + url, e);
        }
    }

}
