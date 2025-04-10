package com.pse.tixclick.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.pse.tixclick.email.EmailService;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.jwt.Jwt;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.payload.entity.entity_enum.ERole;
import com.pse.tixclick.payload.request.IntrospectRequest;
import com.pse.tixclick.payload.request.LoginRequest;
import com.pse.tixclick.payload.response.IntrospectResponse;
import com.pse.tixclick.payload.response.TokenResponse;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.RoleRepository;
import com.pse.tixclick.service.impl.AuthenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenServiceTest {

    @Mock
    Jwt jwt;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private RedisConnection redisConnection;

    @Mock
    private AccountRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    EmailService emailService;

    @InjectMocks
    private AuthenServiceImpl authenService;

    @BeforeEach
    void setUp() {
        // setup redis mock
        lenient().when(stringRedisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
        lenient().when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        lenient().when(redisConnection.ping()).thenReturn("PONG");
    }

    @Test
    void login_ShouldReturnTokenResponse_WhenValidCredentials() {
        // Arrange
        String username = "testuser";
        String rawPassword = "123456";
        String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);
        Role role = new Role();
        role.setRoleName(ERole.BUYER);

        Account user = new Account();
        user.setUserName(username);
        user.setPassword(encodedPassword);
        user.setRole(role);
        user.setActive(true);

        LoginRequest request = new LoginRequest(username, rawPassword);

        TokenResponse expectedResponse = TokenResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .roleName("BUYER")
                .status(true)
                .build();

        Jwt.TokenPair mockTokenPair = new Jwt.TokenPair(
                new Jwt.TokenInfo("access-token", new Date()),
                new Jwt.TokenInfo("refresh-token", new Date())
        );

        // mock repository + jwt
        when(userRepository.findAccountByUserName(username)).thenReturn(Optional.of(user));
        when(jwt.generateTokens(user)).thenReturn(mockTokenPair);

        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(stringRedisTemplate.hasKey("REFRESH_TOKEN:" + username)).thenReturn(false);

        // Act
        TokenResponse result = authenService.login(request);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse.getAccessToken(), result.getAccessToken());
        assertEquals(expectedResponse.getRefreshToken(), result.getRefreshToken());
        assertEquals(expectedResponse.getRoleName(), result.getRoleName());
        assertTrue(result.isStatus());

        verify(stringRedisTemplate).opsForValue();
        verify(valueOperations).set(anyString(), anyString(), eq(7L), eq(TimeUnit.DAYS));
        verify(userRepository).findAccountByUserName(username);
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String username = "invaliduser";
        String password = "password";

        LoginRequest request = new LoginRequest(username, password);

        when(userRepository.findAccountByUserName(username)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authenService.login(request));
        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }
    @Test
    void login_ShouldThrowException_WhenUserIsInactive() {
        // Arrange
        String username = "inactiveUser";
        String rawPassword = "password";
        String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);

        Role role = new Role();
        role.setRoleName(ERole.BUYER);

        Account user = new Account();
        user.setUserName(username);
        user.setPassword(encodedPassword);
        user.setRole(role);
        user.setActive(false); // not active

        LoginRequest request = new LoginRequest(username, rawPassword);

        when(userRepository.findAccountByUserName(username)).thenReturn(Optional.of(user));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authenService.login(request));
        assertEquals(ErrorCode.USER_NOT_ACTIVE, exception.getErrorCode());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsIncorrect() {
        // Arrange
        String username = "testuser";
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";

        String encodedPassword = new BCryptPasswordEncoder().encode(correctPassword);

        Role role = new Role();
        role.setRoleName(ERole.BUYER);

        Account user = new Account();
        user.setUserName(username);
        user.setPassword(encodedPassword);
        user.setRole(role);
        user.setActive(true);

        LoginRequest request = new LoginRequest(username, wrongPassword);

        when(userRepository.findAccountByUserName(username)).thenReturn(Optional.of(user));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authenService.login(request));
        assertEquals(ErrorCode.UNAUTHENTICATED, exception.getErrorCode());
    }
    @Test
    void login_ShouldDeleteOldTokenInRedis_WhenKeyExists() {
        // Arrange
        String username = "testuser";
        String rawPassword = "123456";
        String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);

        Role role = new Role();
        role.setRoleName(ERole.BUYER);

        Account user = new Account();
        user.setUserName(username);
        user.setPassword(encodedPassword);
        user.setRole(role);
        user.setActive(true);

        LoginRequest request = new LoginRequest(username, rawPassword);

        Jwt.TokenPair tokenPair = new Jwt.TokenPair(
                new Jwt.TokenInfo("access-token", new Date()),
                new Jwt.TokenInfo("refresh-token", new Date())
        );

        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);

        when(userRepository.findAccountByUserName(username)).thenReturn(Optional.of(user));
        when(jwt.generateTokens(user)).thenReturn(tokenPair);
        when(stringRedisTemplate.getConnectionFactory()).thenReturn(mock(RedisConnectionFactory.class));
        when(stringRedisTemplate.getConnectionFactory().getConnection()).thenReturn(mock(RedisConnection.class));
        when(stringRedisTemplate.hasKey("REFRESH_TOKEN:" + username)).thenReturn(true); // key exists
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act
        TokenResponse result = authenService.login(request);

        // Assert
        verify(stringRedisTemplate).delete("REFRESH_TOKEN:" + username);
        verify(stringRedisTemplate.opsForValue()).set(anyString(), anyString(), eq(7L), eq(TimeUnit.DAYS));
    }
    @Test
    void login_ShouldNotDeleteOldTokenInRedis_WhenKeyDoesNotExist() {
        // Arrange
        String username = "testuser";
        String rawPassword = "123456";
        String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);

        Role role = new Role();
        role.setRoleName(ERole.BUYER);

        Account user = new Account();
        user.setUserName(username);
        user.setPassword(encodedPassword);
        user.setRole(role);
        user.setActive(true);

        LoginRequest request = new LoginRequest(username, rawPassword);

        Jwt.TokenPair tokenPair = new Jwt.TokenPair(
                new Jwt.TokenInfo("access-token", new Date()),
                new Jwt.TokenInfo("refresh-token", new Date())
        );

        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);

        when(userRepository.findAccountByUserName(username)).thenReturn(Optional.of(user));
        when(jwt.generateTokens(user)).thenReturn(tokenPair);
        when(stringRedisTemplate.getConnectionFactory()).thenReturn(mock(RedisConnectionFactory.class));
        when(stringRedisTemplate.getConnectionFactory().getConnection()).thenReturn(mock(RedisConnection.class));
        when(stringRedisTemplate.hasKey("REFRESH_TOKEN:" + username)).thenReturn(false); // key not found
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act
        TokenResponse result = authenService.login(request);

        // Assert
        verify(stringRedisTemplate, never()).delete("REFRESH_TOKEN:" + username);
        verify(stringRedisTemplate.opsForValue()).set(anyString(), anyString(), eq(7L), eq(TimeUnit.DAYS));
    }
    @Test
    void introspect_ShouldReturnValidTrue_WhenTokenIsValid() throws Exception {
        // Arrange
        String token = createValidToken(); // generate a real JWT with valid expiration

        IntrospectRequest request = new IntrospectRequest(token);

        SignedJWT signedJWT = SignedJWT.parse(token);
        when(jwt.verifyToken(token)).thenReturn(signedJWT);

        // Act
        IntrospectResponse response = authenService.introspect(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isValid());
    }

    private String createValidToken() throws JOSEException {
        JWSSigner signer = new MACSigner("your-secret-signing-key-which-is-at-least-256-bit");
        Date now = new Date();
        Date exp = new Date(now.getTime() + 3600000); // 1 giờ sau

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("testuser")
                .expirationTime(exp)
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claimsSet
        );

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }
    @Test
    void introspect_ShouldReturnValidFalse_WhenAppExceptionThrown() throws Exception {
        // Arrange
        String token = "invalid.token.app";
        IntrospectRequest request = new IntrospectRequest(token);

        when(jwt.verifyToken(token)).thenThrow(new AppException(ErrorCode.INVALID_TOKEN));

        // Act
        IntrospectResponse response = authenService.introspect(request);

        // Assert
        assertNotNull(response);
        assertFalse(response.isValid());
    }
    @Test
    void introspect_ShouldReturnValidFalse_WhenJOSEExceptionThrown() throws Exception {
        // Arrange
        String token = "invalid.token.jose";
        IntrospectRequest request = new IntrospectRequest(token);

        when(jwt.verifyToken(token)).thenThrow(new JOSEException("JOSE error"));

        // Act
        IntrospectResponse response = authenService.introspect(request);

        // Assert
        assertNotNull(response);
        assertFalse(response.isValid());
    }

    @Test
    void introspect_ShouldReturnValidFalse_WhenParseExceptionThrown() throws Exception {
        // Arrange
        String token = "invalid.token.parse";
        IntrospectRequest request = new IntrospectRequest(token);

        when(jwt.verifyToken(token)).thenThrow(new ParseException("Parse error", 0));

        // Act
        IntrospectResponse response = authenService.introspect(request);

        // Assert
        assertNotNull(response);
        assertFalse(response.isValid());
    }

    @Test
    void introspect_ShouldReturnValidFalse_WhenUnexpectedExceptionThrown() throws Exception {
        // Arrange
        String token = "invalid.token.unknown";
        IntrospectRequest request = new IntrospectRequest(token);

        when(jwt.verifyToken(token)).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        IntrospectResponse response = authenService.introspect(request);

        // Assert
        assertNotNull(response);
        assertFalse(response.isValid());
    }
}
