package com.example.demo.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    public static final String KAKAO_ISSUER = "https://kauth.kakao.com";

    @Bean
    public RestClient googleOauthRestClient() {
        return RestClient.builder()
                .baseUrl("https://oauth2.googleapis.com")
                .build();
    }

    @Bean
    public RestClient kakaoTokenRestClient() {
        return RestClient.builder()
                .baseUrl(KAKAO_ISSUER)
                .build();
    }
}
