package com.pse.tixclick.service;

import com.nimbusds.jose.JOSEException;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.request.IntrospectRequest;
import com.pse.tixclick.payload.request.LoginRequest;
import com.pse.tixclick.payload.request.SignUpRequest;
import com.pse.tixclick.payload.response.GetToken;
import com.pse.tixclick.payload.response.IntrospectResponse;
import com.pse.tixclick.payload.response.RefreshTokenResponse;
import com.pse.tixclick.payload.response.TokenResponse;
import jakarta.mail.MessagingException;

import java.text.ParseException;

public interface AuthenService {
    TokenResponse login(LoginRequest loginRequest);
    IntrospectResponse introspect(IntrospectRequest introspectRequest);

    RefreshTokenResponse refreshToken(IntrospectRequest refreshToken) throws JOSEException, ParseException;

    boolean register(SignUpRequest signUpRequest);

    void createAndSendOTP(String email) throws MessagingException;
    boolean verifyOTP(String email, String otpCode);

    GetToken getToken();
}
