package com.pse.tixclick.service;

import com.nimbusds.jose.JOSEException;
import com.pse.tixclick.payload.request.IntrospectRequest;
import com.pse.tixclick.payload.request.LoginRequest;
import com.pse.tixclick.payload.request.SignUpRequest;
import com.pse.tixclick.payload.response.GetToken;
import com.pse.tixclick.payload.response.IntrospectResponse;
import com.pse.tixclick.payload.response.RefreshTokenResponse;
import com.pse.tixclick.payload.response.TokenResponse;
import jakarta.mail.MessagingException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.text.ParseException;

public interface AuthenService {
    TokenResponse login(LoginRequest loginRequest);
    IntrospectResponse introspect(IntrospectRequest introspectRequest);

    RefreshTokenResponse refreshToken(IntrospectRequest refreshToken) throws JOSEException, ParseException;

    boolean register(SignUpRequest signUpRequest) throws MessagingException;

    void createAndSendOTP(String email) throws MessagingException;
    TokenResponse verifyOTP(String email, String otpCode);

    GetToken getToken();

    TokenResponse signupAndLoginWithGitHub(@AuthenticationPrincipal OAuth2User principal);

    TokenResponse signupAndLoginWithGoogle(@AuthenticationPrincipal OAuth2User principal);

    TokenResponse loginWithManagerAndAdmin(LoginRequest loginRequest);

}
