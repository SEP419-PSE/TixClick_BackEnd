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
import com.pse.tixclick.payload.request.SignUpRequest;
import com.pse.tixclick.payload.response.IntrospectResponse;
import com.pse.tixclick.payload.response.RefreshTokenResponse;
import com.pse.tixclick.payload.response.TokenResponse;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.RoleRepository;
import com.pse.tixclick.service.impl.AuthenServiceImpl;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
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

    @Spy
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
    void login_ShouldReturnTokenResponseWithInactiveStatus_WhenUserIsInactive() {
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
        user.setEmail("inactive@example.com");

        LoginRequest request = new LoginRequest(username, rawPassword);

        when(userRepository.findAccountByUserName(username)).thenReturn(Optional.of(user));

        // Act
        TokenResponse response = authenService.login(request);

        // Assert
        assertFalse(response.isStatus());
        assertEquals("inactive@example.com", response.getEmail());
        assertNull(response.getAccessToken()); // vì không generate token nếu inactive
        assertNull(response.getRefreshToken());
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

    @Test
    void refreshToken_ShouldReturnAccessTokenOnly_WhenExpirationTimeMoreThanOneDay() throws Exception {
        String refreshToken = "valid-refresh-token";
        IntrospectRequest request = new IntrospectRequest(refreshToken);

        Account account = new Account();
        account.setAccountId(1);
        account.setUserName("testuser");

        Jwt.TokenPair tokenPair = new Jwt.TokenPair(
                new Jwt.TokenInfo("new-access-token", new Date(System.currentTimeMillis() + 60000)),
                new Jwt.TokenInfo("new-refresh-token", new Date(System.currentTimeMillis() + 604800000))
        );

        // Mock redis
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("REFRESH_TOKEN:testuser")).thenReturn(refreshToken);
        when(stringRedisTemplate.getExpire("REFRESH_TOKEN:testuser", TimeUnit.SECONDS)).thenReturn(86401L);

        // Mock jwt and repository
        when(jwt.extractUserId(refreshToken)).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(account));
        when(jwt.generateTokens(account)).thenReturn(tokenPair);

        // Act
        RefreshTokenResponse response = authenService.refreshToken(request);

        // Assert
        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertNull(response.getRefreshToken()); // Không cập nhật refresh token mới
    }

    @Test
    void refreshToken_ShouldReturnNewRefreshToken_WhenExpirationTimeLessThanOneDay() throws Exception {
        String refreshToken = "valid-refresh-token";
        IntrospectRequest request = new IntrospectRequest(refreshToken);

        Account account = new Account();
        account.setAccountId(1);
        account.setUserName("testuser");

        Jwt.TokenPair tokenPair = new Jwt.TokenPair(
                new Jwt.TokenInfo("new-access-token", new Date()),
                new Jwt.TokenInfo("new-refresh-token", new Date())
        );

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("REFRESH_TOKEN:testuser")).thenReturn(refreshToken);
        when(stringRedisTemplate.getExpire("REFRESH_TOKEN:testuser", TimeUnit.SECONDS)).thenReturn(1000L);

        when(jwt.extractUserId(refreshToken)).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(account));
        when(jwt.generateTokens(account)).thenReturn(tokenPair);

        RefreshTokenResponse response = authenService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());

        verify(valueOperations).set(eq("REFRESH_TOKEN:testuser"), eq("new-refresh-token"), eq(7L), eq(TimeUnit.DAYS));
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenNotInRedis() throws Exception {
        String refreshToken = "expired-refresh-token";
        IntrospectRequest request = new IntrospectRequest(refreshToken);

        Account account = new Account();
        account.setAccountId(1);
        account.setUserName("testuser");

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("REFRESH_TOKEN:testuser")).thenReturn(null); // Không có token

        when(jwt.extractUserId(refreshToken)).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(account));

        AppException ex = assertThrows(AppException.class, () -> authenService.refreshToken(request));
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, ex.getErrorCode());
    }

    @Test
    void refreshToken_ShouldThrowException_WhenUserNotFound() throws Exception {
        String refreshToken = "valid-refresh-token";
        IntrospectRequest request = new IntrospectRequest(refreshToken);

        when(jwt.extractUserId(refreshToken)).thenReturn(99);
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> authenService.refreshToken(request));
        assertEquals(ErrorCode.USER_NOT_EXISTED, ex.getErrorCode());
    }
    @Test
    void refreshToken_ShouldThrowException_WhenAccountUsernameIsNull() throws Exception {
        String refreshToken = "valid-refresh-token";
        IntrospectRequest request = new IntrospectRequest(refreshToken);

        Account account = new Account(); // không set userName -> null
        account.setAccountId(1);

        when(jwt.extractUserId(refreshToken)).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(account));

        AppException ex = assertThrows(AppException.class, () -> authenService.refreshToken(request));
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, ex.getErrorCode());
    }

    @Test
    void register_ShouldSucceed_WhenValidRequest() throws MessagingException {
        // Arrange
        SignUpRequest request = SignUpRequest.builder()
                .userName("testuser")
                .password("password123")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        when(userRepository.findAccountByUserName("testuser")).thenReturn(Optional.empty());
        when(userRepository.findAccountByEmail("test@example.com")).thenReturn(Optional.empty());

        Role buyerRole = new Role();
        buyerRole.setRoleName(ERole.BUYER);
        when(roleRepository.findRoleByRoleName(ERole.BUYER)).thenReturn(Optional.of(buyerRole));

        when(userRepository.saveAndFlush(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Bypass send OTP
        doNothing().when(authenService).createAndSendOTP("test@example.com");

        // Act
        boolean result = authenService.register(request);

        // Assert
        assertTrue(result);
        verify(userRepository).saveAndFlush(any(Account.class));
        verify(userRepository).findAccountByUserName("testuser");
        verify(userRepository).findAccountByEmail("test@example.com");
        verify(roleRepository).findRoleByRoleName(ERole.BUYER);
        verify(authenService).createAndSendOTP("test@example.com");
        verifyNoMoreInteractions(userRepository, roleRepository);
    }


    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        SignUpRequest request = SignUpRequest.builder()
                .userName("existingUser")
                .password("password")
                .email("test@example.com")
                .build();

        when(userRepository.findAccountByUserName("existingUser"))
                .thenReturn(Optional.of(new Account()));

        AppException exception = assertThrows(AppException.class, () -> authenService.register(request));
        assertEquals(ErrorCode.USERNAME_TAKEN, exception.getErrorCode());
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        SignUpRequest request = SignUpRequest.builder()
                .userName("newUser")
                .password("password")
                .email("existing@example.com")
                .build();

        when(userRepository.findAccountByUserName("newUser")).thenReturn(Optional.empty());
        when(userRepository.findAccountByEmail("existing@example.com"))
                .thenReturn(Optional.of(new Account()));

        AppException exception = assertThrows(AppException.class, () -> authenService.register(request));
        assertEquals(ErrorCode.EMAIL_TAKEN, exception.getErrorCode());
    }

    @Test
    void register_ShouldThrowException_WhenRoleNotFound() {
        SignUpRequest request = SignUpRequest.builder()
                .userName("user123")
                .password("pass")
                .email("abc@example.com")
                .build();

        when(userRepository.findAccountByUserName("user123")).thenReturn(Optional.empty());
        when(userRepository.findAccountByEmail("abc@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findRoleByRoleName(ERole.BUYER)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> authenService.register(request));
        assertEquals(ErrorCode.ROLE_NOT_EXISTED, exception.getErrorCode());
    }


    @Test
    void createAndSendOTP_ShouldStoreOTPAndSendEmail_WhenValidRequest() throws MessagingException {
        // Arrange
        String email = "test@example.com";
        Account user = new Account();
        user.setEmail(email);
        user.setUserName("testuser");
        user.setActive(false);

        when(userRepository.findAccountByEmail(email)).thenReturn(Optional.of(user));

        // Mock Redis operations
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        // Không có thời gian gửi trước đó -> hợp lệ để gửi mới
        when(valueOperations.get("OTP_TIME:" + email)).thenReturn(null);

        // Act
        authenService.createAndSendOTP(email);

        // Assert
        verify(stringRedisTemplate, atLeastOnce()).opsForValue();

        // Verify OTP và TIME đều được set trong Redis
        verify(valueOperations).set(startsWith("OTP:"), anyString(), eq(15L), eq(TimeUnit.MINUTES));
        verify(valueOperations).set(startsWith("OTP_TIME:"), anyString(), eq(15L), eq(TimeUnit.MINUTES));

        // Verify email được gửi
        verify(emailService).sendOTPtoActiveAccount(eq(email), anyString(), eq("testuser"));
    }

    @Test
    void createAndSendOTP_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String email = "notfound@example.com";
        when(userRepository.findAccountByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authenService.createAndSendOTP(email));
        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }
    @Test
    void createAndSendOTP_ShouldThrowException_WhenUserIsActive() {
        // Arrange
        String email = "active@example.com";
        Account user = new Account();
        user.setEmail(email);
        user.setActive(true); // already active

        when(userRepository.findAccountByEmail(email)).thenReturn(Optional.of(user));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authenService.createAndSendOTP(email));
        assertEquals(ErrorCode.USER_ACTIVE, exception.getErrorCode());
    }
    @Test
    void createAndSendOTP_ShouldThrowException_WhenOTPSentRecently() {
        // Arrange
        String email = "test@example.com";
        Account user = new Account();
        user.setEmail(email);
        user.setUserName("testuser");
        user.setActive(false);

        when(userRepository.findAccountByEmail(email)).thenReturn(Optional.of(user));

        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        // Giả lập thời gian gửi gần nhất cách hiện tại chưa tới 30s
        long now = System.currentTimeMillis();
        when(valueOperations.get("OTP_TIME:" + email)).thenReturn(String.valueOf(now - 10_000)); // chỉ mới 10s trước

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authenService.createAndSendOTP(email));
        assertEquals(ErrorCode.OTP_ALREADY_SENT_RECENTLY, exception.getErrorCode());
    }

    @Test
    void verifyOTP_ShouldReturnTokenResponse_WhenOTPIsValid() {
        // Arrange
        String email = "test@example.com";
        String otp = "123456";
        String accessToken = "access-token-example";
        String refreshToken = "refresh-token-example";
        String redisKey = "REFRESH_TOKEN:testuser";

        Account user = new Account();
        user.setEmail(email);
        user.setUserName("testuser");
        user.setActive(false);
        Role role = new Role();
        role.setRoleName(ERole.BUYER);
        user.setRole(role);

        Jwt.TokenInfo accessTokenInfo = new Jwt.TokenInfo(accessToken, new Date());
        Jwt.TokenInfo refreshTokenInfo = new Jwt.TokenInfo(refreshToken, new Date());
        Jwt.TokenPair tokenPair = new Jwt.TokenPair(accessTokenInfo, refreshTokenInfo);

        // Mock Redis
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("OTP:" + email)).thenReturn(otp);

        // Mock repo & JWT
        when(userRepository.findAccountByEmail(email)).thenReturn(Optional.of(user));
        when(jwt.generateTokens(user)).thenReturn(tokenPair);

        // Act
        TokenResponse result = authenService.verifyOTP(email, otp);

        // Assert
        assertNotNull(result);
        assertEquals(accessToken, result.getAccessToken());
        assertEquals(refreshToken, result.getRefreshToken());
        assertTrue(result.isStatus());
        assertEquals("BUYER", result.getRoleName());

        verify(userRepository).save(user);
        verify(stringRedisTemplate).delete("OTP:" + email);
        verify(stringRedisTemplate.opsForValue()).set(eq(redisKey), eq(refreshToken), eq(7L), eq(TimeUnit.DAYS));
    }



    @Test
    void verifyOTP_ShouldThrowException_WhenOTPIsIncorrect() {
        // Arrange
        String email = "test@example.com";
        String correctOtp = "123456";
        String wrongOtp = "999999";

        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("OTP:" + email)).thenReturn(correctOtp);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            authenService.verifyOTP(email, wrongOtp);
        });

        assertEquals(ErrorCode.INVALID_OTP, exception.getErrorCode());
        verify(userRepository, never()).save(any());
        verify(stringRedisTemplate, never()).delete(anyString());
    }


    @Test
    void verifyOTP_ShouldThrowException_WhenOTPIsNotFound() {
        // Arrange
        String email = "test@example.com";
        String otp = "123456";

        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("OTP:" + email)).thenReturn(null); // OTP không tồn tại

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            authenService.verifyOTP(email, otp);
        });

        assertEquals(ErrorCode.INVALID_OTP, exception.getErrorCode());
        verify(userRepository, never()).save(any());
        verify(stringRedisTemplate, never()).delete(anyString());
    }


    @Test
    void verifyOTP_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String email = "test@example.com";
        String otp = "123456";

        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("OTP:" + email)).thenReturn(otp);

        when(userRepository.findAccountByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authenService.verifyOTP(email, otp));
        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void loginWithManagerAndAdmin_ShouldReturnToken_WhenValidManagerAccount() {
        // Arrange
        String username = "managerUser";
        String rawPassword = "password123";
        String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);

        Role role = new Role();
        role.setRoleName(ERole.MANAGER);

        Account user = new Account();
        user.setUserName(username);
        user.setPassword(encodedPassword);
        user.setActive(true);
        user.setRole(role);

        LoginRequest request = new LoginRequest(username, rawPassword);

        when(userRepository.findAccountByUserName(username)).thenReturn(Optional.of(user));

        // Tạo TokenPair giả
        Jwt.TokenInfo accessToken = new Jwt.TokenInfo ("mockAccessToken", new Date(System.currentTimeMillis() + 3600 * 1000));
        Jwt.TokenInfo refreshToken = new Jwt.TokenInfo ("mockRefreshToken", new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000));
        Jwt.TokenPair mockTokenPair = new Jwt.TokenPair(accessToken, refreshToken);

        when(jwt.generateTokens(user)).thenReturn(mockTokenPair);

        // Act
        TokenResponse response = authenService.loginWithManagerAndAdmin(request);

        // Assert
        assertEquals("mockAccessToken", response.getAccessToken());
        assertEquals("mockRefreshToken", response.getRefreshToken());
        assertTrue(response.isStatus());
        assertEquals("MANAGER", response.getRoleName());
    }




}
