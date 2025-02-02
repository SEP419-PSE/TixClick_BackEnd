package com.pse.tixclick.jwt;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.entity.Account;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Component
public class Jwt {

    @Value("${app.jwt-secret}")
    private String signerKey;

    @Value("${app.jwt-access-expiration-milliseconds}")
    private long accessExpirationMillis;

    @Value("${app.jwt-refresh-expiration-milliseconds}")
    private long refreshExpirationMillis;

    public TokenPair generateTokens(Account user) {
        // Generate Access Token
        TokenInfo accessToken = generateToken(user, accessExpirationMillis);

        // Generate Refresh Token
        TokenInfo refreshToken = generateRefreshToken(user);

        return new TokenPair(accessToken, refreshToken);
    }

    public  TokenInfo generateAccessToken(Account user) {
        return generateToken(user, accessExpirationMillis);
    }

    // Function to generate both Access and Refresh tokens
    private TokenInfo generateToken(Account user, long expirationMillis) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

            Date issueTime = new Date();
            Date expiryTime = new Date(issueTime.getTime() + expirationMillis);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUserName())
                    .issuer("devteria.com")
                    .issueTime(issueTime)
                    .expirationTime(expiryTime)
                    .jwtID(UUID.randomUUID().toString())
                    .claim("userId", user.getAccountId())  // Store only essential information
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(new MACSigner(signerKey.getBytes()));

            return new TokenInfo(signedJWT.serialize(), expiryTime);
        } catch (JOSEException e) {
            throw new RuntimeException("Error creating JWT token", e);
        }
    }

    // New function to generate Refresh Token
    private TokenInfo generateRefreshToken(Account user) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

            Date issueTime = new Date();
            Date expiryTime = new Date(issueTime.getTime() + refreshExpirationMillis);

            // Only store userId and jwtID in the refresh token
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .issuer("namphan.com")
                    .issueTime(issueTime)
                    .expirationTime(expiryTime)
                    .jwtID(UUID.randomUUID().toString())  // Use a shorter, unique identifier for refresh tokens
                    .claim("userId", user.getAccountId())  // Store only the userId or reference
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(new MACSigner(signerKey.getBytes()));

            return new TokenInfo(signedJWT.serialize(), expiryTime);
        } catch (JOSEException e) {
            throw new RuntimeException("Error creating refresh JWT token", e);
        }
    }

    // Function to verify the token
    public SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        return signedJWT;
    }

    public record TokenInfo(String token, Date expiryDate) {}

    public record TokenPair(TokenInfo accessToken, TokenInfo refreshToken) {}
}
