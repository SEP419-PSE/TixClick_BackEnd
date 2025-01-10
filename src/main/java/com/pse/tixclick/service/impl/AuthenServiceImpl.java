package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.jwt.Jwt;
import com.pse.tixclick.payload.entity.RefreshToken;
import com.pse.tixclick.payload.request.IntrospectRequest;
import com.pse.tixclick.payload.request.LoginRequest;
import com.pse.tixclick.payload.response.IntrospectResponse;
import com.pse.tixclick.payload.response.RefreshTokenResponse;
import com.pse.tixclick.payload.response.TokenResponse;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.RefreshTokenRepository;
import com.pse.tixclick.service.AccountService;
import com.pse.tixclick.service.AuthenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenServiceImpl implements AuthenService {
    Jwt jwt;
    AccountRepository userRepository;
    RefreshTokenRepository refreshTokenRepository;
    @Override
    public TokenResponse login(LoginRequest loginRequest) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository
                .findAccountByUserName(loginRequest.getUserName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        boolean authenticated = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        RefreshToken refreshTokenold = refreshTokenRepository.findRefreshTokenByUserName(user.getUserName())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));
        refreshTokenRepository.delete(refreshTokenold);

        var tokenPair = jwt.generateTokens(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenPair.refreshToken().token());
        refreshToken.setExpiryDate(tokenPair.refreshToken().expiryDate().toInstant());
        refreshToken.setUserName(user.getUserName());
        refreshTokenRepository.save(refreshToken);

        return TokenResponse.builder()
                .accessToken(tokenPair.accessToken().token())
                .refreshToken(tokenPair.refreshToken().token())
                .accessExpiryTime(tokenPair.accessToken().expiryDate())
                .refreshExpiryTime(tokenPair.refreshToken().expiryDate())
                .build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest introspectRequest) {
        return null;
    }

    @Override
    public RefreshTokenResponse refreshToken(IntrospectRequest refreshToken) {
        return null;
    }
}
