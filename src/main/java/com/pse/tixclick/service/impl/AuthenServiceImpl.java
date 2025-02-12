package com.pse.tixclick.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.jwt.Jwt;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.RefreshToken;
import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.payload.request.IntrospectRequest;
import com.pse.tixclick.payload.request.LoginRequest;
import com.pse.tixclick.payload.request.SignUpRequest;
import com.pse.tixclick.payload.response.GetToken;
import com.pse.tixclick.payload.response.IntrospectResponse;
import com.pse.tixclick.payload.response.RefreshTokenResponse;
import com.pse.tixclick.payload.response.TokenResponse;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.RefreshTokenRepository;
import com.pse.tixclick.repository.RoleRepository;
import com.pse.tixclick.service.AccountService;
import com.pse.tixclick.service.AuthenService;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenServiceImpl implements AuthenService {
    private final ConcurrentHashMap<String, String> otpStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> otpExpirationStore = new ConcurrentHashMap<>();  // Để lưu thời gian hết hạ

    @Autowired
    Jwt jwt;
    @Autowired
    AccountRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Value("${app.jwt-secret}")
    private String SIGNER_KEY;
    @Override
    public TokenResponse login(LoginRequest loginRequest) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        // Kiểm tra người dùng có tồn tại hay không
        var user = userRepository
                .findAccountByUserName(loginRequest.getUserName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Kiểm tra mật khẩu hợp lệ
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Xóa refresh token cũ (nếu có)
        RefreshToken refreshTokenOld = refreshTokenRepository.findRefreshTokenByUserName(user.getUserName())
                .orElse(null); // Có thể trả về null nếu không có refresh token cũ

        if (refreshTokenOld != null) {
            refreshTokenRepository.delete(refreshTokenOld);
        }

        // Tạo mới bộ token (access token và refresh token)
        var tokenPair = jwt.generateTokens(user);

        // Tạo mới refresh token và lưu vào cơ sở dữ liệu
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenPair.refreshToken().token());
        refreshToken.setExpiryDate(tokenPair.refreshToken().expiryDate().toInstant());
        refreshToken.setUserName(user.getUserName());
        refreshTokenRepository.save(refreshToken);

        // Trả về TokenResponse chứa access token và refresh token
        return TokenResponse.builder()
                .accessToken(tokenPair.accessToken().token())
                .refreshToken(tokenPair.refreshToken().token())
                .status(user.isActive())
                .build();
    }


    @Override
    public IntrospectResponse introspect(IntrospectRequest introspectRequest) {
        var token = introspectRequest.getToken();
        boolean isValid = true;

        try {
            // Kiểm tra tính hợp lệ của token
            jwt.verifyToken(token);
        } catch (AppException e) {
            // Xử lý lỗi AppException
            isValid = false;
        } catch (JOSEException e) {
            // Xử lý lỗi JOSEException
            isValid = false;
        } catch (ParseException e) {
            // Xử lý lỗi ParseException
            isValid = false;
        } catch (Exception e) {
            // Bắt tất cả các lỗi không xác định
            isValid = false;
        }

        // Trả về IntrospectResponse với trạng thái valid và thông báo lỗi nếu có
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }


    @Override
    public RefreshTokenResponse refreshToken(IntrospectRequest refreshTokenRequest) throws JOSEException, ParseException {

        RefreshToken refreshToken = refreshTokenRepository.findRefreshTokenByToken(refreshTokenRequest.getToken())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));

        // Kiểm tra xem refresh token có còn hợp lệ không
        boolean isTokenValid = jwt.verifyToken(refreshToken.getToken()) != null;
        if (!isTokenValid) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Lấy user từ refresh token
        String userName = refreshToken.getUserName();
        var user = userRepository
                .findAccountByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Xóa refresh token cũ khỏi cơ sở dữ liệu
        refreshTokenRepository.delete(refreshToken);

        // Tạo refresh token mới và access token mới
        var tokenPair = jwt.generateTokens(user);

        // Lưu refresh token mới vào cơ sở dữ liệu
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(tokenPair.refreshToken().token());
        newRefreshToken.setExpiryDate(tokenPair.refreshToken().expiryDate().toInstant());
        newRefreshToken.setUserName(user.getUserName());
        refreshTokenRepository.save(newRefreshToken);

        // Trả về access token và thời gian hết hạn của access token
        return RefreshTokenResponse.builder()
                .accessToken(tokenPair.accessToken().token())
                .accessExpiryTime(tokenPair.accessToken().expiryDate())
                .build();
    }

    @Override
    public boolean


    register(SignUpRequest signUpRequest) {

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);




        // Kiểm tra xem username đã tồn tại chưa
        if (userRepository.findAccountByUserName(signUpRequest.getUserName()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Kiểm tra xem email đã tồn tại chưa
        if (userRepository.findAccountByEmail(signUpRequest.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_TAKEN);
        }
        Role role = roleRepository.findRoleByRoleName("BUYER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        // Nếu tất cả hợp lệ, tiếp tục tạo tài khoản mới
        Account newUser = new Account();
        newUser.setUserName(signUpRequest.getUserName());
        newUser.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        newUser.setEmail(signUpRequest.getEmail());
        newUser.setFirstName(signUpRequest.getFirstName());
        newUser.setLastName(signUpRequest.getLastName());
        newUser.setActive(false);
        newUser.setRole(role);

        userRepository.save(newUser);
        return true;
    }

    @Override
    public void createAndSendOTP(String email) throws MessagingException {
        String otpCode = generateOTP();  // Tạo OTP

        // Kiểm tra người dùng có tồn tại trong cơ sở dữ liệu không
        var user = userRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if(user.isActive() == true){
            throw new AppException(ErrorCode.USER_ACTIVE);
        }
        // Lưu OTP vào bộ nhớ (ConcurrentHashMap) với thời gian hết hạn là 5 phút
        otpStore.put(email, otpCode);
        otpExpirationStore.put(email, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)); // Hết hạn sau 5 phút

        // Gửi OTP qua email
        String message = "Your OTP code is: " + otpCode;
        emailService.sendOTPtoActiveAccount(email, otpCode, user.getUserName());
    }

    @Override
    public boolean verifyOTP(String email, String otpCode) {
        // Lấy mã OTP đã lưu và thời gian hết hạn từ bộ lưu trữ
        String storedOtp = otpStore.get(email);
        Long expirationTime = otpExpirationStore.get(email);

        // Kiểm tra nếu OTP không tồn tại hoặc đã hết hạn
        if (storedOtp == null || expirationTime == null || System.currentTimeMillis() > expirationTime) {
            // Xóa OTP khỏi bộ nhớ khi hết hạn hoặc không tồn tại
            otpStore.remove(email);
            otpExpirationStore.remove(email);
            return false;  // OTP không tồn tại hoặc đã hết hạn
        }

        // Kiểm tra mã OTP
        boolean isValid = storedOtp.equals(otpCode);

        // Nếu OTP đã được xác thực, xóa OTP khỏi bộ nhớ
        if (isValid) {
            otpStore.remove(email);
            otpExpirationStore.remove(email);
        }

        var user = userRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setActive(true);
        userRepository.save(user);

        return isValid;  // Trả về true nếu OTP hợp lệ, false nếu không hợp lệ
    }

    @Override
    public GetToken getToken() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        var user = userRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        GetToken getToken = new GetToken();
        getToken.setRoleName(user.getRole().getRoleName());
        getToken.setUserName(user.getUserName());
        return getToken;
    }

    @Override
    @Transactional
    public TokenResponse signupAndLoginWithGitHub(OAuth2User principal) {
        String username = principal.getAttribute("login");
        String fullName = principal.getAttribute("name");
        String email = principal.getAttribute("email");
        String avatarUrl = principal.getAttribute("avatar_url");



        // Kiểm tra tài khoản đã tồn tại chưa
        Account user = userRepository.findAccountByUserName(username).orElse(null);

        if (user == null) {
            // Lấy role "BUYER"
            Role role = roleRepository.findRoleByRoleName("BUYER")
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

            // Tạo mới tài khoản
            user = new Account();
            user.setUserName(username);
            user.setEmail("none");
            user.setRole(role);
            user.setPassword("github");
            user.setActive(true);
            userRepository.save(user); // Lưu vào database
        }

        // Xóa refresh token cũ nếu tồn tại
        refreshTokenRepository.findRefreshTokenByUserName(user.getUserName())
                .ifPresent(refreshTokenRepository::delete);

        // Tạo mới bộ token (access token & refresh token)
        var tokenPair = jwt.generateTokens(user);

        // Lưu refresh token mới
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenPair.refreshToken().token());
        refreshToken.setExpiryDate(tokenPair.refreshToken().expiryDate().toInstant());
        refreshToken.setUserName(user.getUserName());
        refreshTokenRepository.save(refreshToken);

        // Trả về TokenResponse chứa access token và refresh token
        return TokenResponse.builder()
                .accessToken(tokenPair.accessToken().token())
                .refreshToken(tokenPair.refreshToken().token())
                .build();
    }

    @Override
    @Transactional
    public TokenResponse signupAndLoginWithFacebook(OAuth2User principal) {
        try {
            String fullName = principal.getAttribute("name");
            String email = principal.getAttribute("email");

            if (email == null || fullName == null) {
                throw new AppException(ErrorCode.FACEBOOK_LOGIN_FAILED);
            }

            // Kiểm tra tài khoản đã tồn tại chưa
            Account user = userRepository.findAccountByUserName(email).orElse(null);

            if (user == null) {
                Role role = roleRepository.findRoleByRoleName("BUYER")
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

                // Tạo tài khoản mới
                user = new Account();
                user.setUserName(email);
                user.setEmail(email);
                user.setRole(role);
                user.setPassword("facebook");
                user.setActive(true);
                userRepository.save(user);
            }

            // Xóa refresh token cũ nếu có
            refreshTokenRepository.findRefreshTokenByUserName(user.getUserName())
                    .ifPresent(refreshTokenRepository::delete);

            // Tạo token mới
            var tokenPair = jwt.generateTokens(user);

            // Lưu refresh token mới
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken(tokenPair.refreshToken().token());
            refreshToken.setExpiryDate(tokenPair.refreshToken().expiryDate().toInstant());
            refreshToken.setUserName(user.getUserName());
            refreshTokenRepository.save(refreshToken);

            return TokenResponse.builder()
                    .accessToken(tokenPair.accessToken().token())
                    .refreshToken(tokenPair.refreshToken().token())
                    .status(user.isActive())
                    .build();

        } catch (AppException e) {
            throw e; // Ném lại để controller xử lý
        } catch (Exception e) {
            throw new AppException(ErrorCode.FACEBOOK_LOGIN_FAILED);
        }
    }




    public String generateOTP() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));  // Tạo OTP 6 chữ số
    }
}
