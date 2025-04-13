package com.pse.tixclick.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/test-oauth2")
public class OAuth2TestController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @GetMapping("/check-login")
    public ResponseEntity<String> checkGoogleLogin() {
        // Giả lập gửi yêu cầu đến Google OAuth2 (chỉ kiểm tra phản hồi)
        String googleOAuthUrl = String.format(
                "https://accounts.google.com/o/oauth2/v2/auth?response_type=code&client_id=%s&redirect_uri=%s&scope=profile%%20email",
                clientId, redirectUri
        );

        // Sử dụng RestTemplate để gửi yêu cầu tới Google (mô phỏng yêu cầu đăng nhập)
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(googleOAuthUrl, String.class);

        return ResponseEntity.ok("Google OAuth2 login check completed. Redirect URL: " + googleOAuthUrl);
    }
}