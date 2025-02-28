package com.pse.tixclick.service.impl;

import com.nimbusds.jose.JOSEException;
import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.jwt.Jwt;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.payload.request.IntrospectRequest;
import com.pse.tixclick.payload.request.LoginRequest;
import com.pse.tixclick.payload.request.SignUpRequest;
import com.pse.tixclick.payload.response.GetToken;
import com.pse.tixclick.payload.response.IntrospectResponse;
import com.pse.tixclick.payload.response.RefreshTokenResponse;
import com.pse.tixclick.payload.response.TokenResponse;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.CompanyAccountRepository;
import com.pse.tixclick.repository.RoleRepository;
import com.pse.tixclick.service.AuthenService;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenServiceImpl implements AuthenService {
    private final ConcurrentHashMap<String, String> otpStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> otpExpirationStore = new ConcurrentHashMap<>();  // Để lưu thời gian hết hạ

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    Jwt jwt;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    AccountRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    CompanyAccountRepository companyAccountRepository;

    @Value("${app.jwt-secret}")
    private String SIGNER_KEY;
    @Override
    public TokenResponse login(LoginRequest loginRequest) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        // Kiểm tra người dùng có tồn tại không
        var user = userRepository
                .findAccountByUserName(loginRequest.getUserName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Kiểm tra mật khẩu hợp lệ
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Xóa Refresh Token cũ trên Redis (nếu có)
        String key = "REFRESH_TOKEN:" + user.getUserName();
        redisTemplate.delete(key);

        // Tạo mới Access Token & Refresh Token
        var tokenPair = jwt.generateTokens(user);

        // Lưu Refresh Token vào Redis với thời gian hết hạn 7 ngày (1 tuần)
        long expirationDays = 7; // 7 ngày
        redisTemplate.opsForValue().set(key, tokenPair.refreshToken().token(), expirationDays, TimeUnit.DAYS);


        // Trả về TokenResponse chứa Access Token & Refresh Token
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
        // Lấy refresh token từ request
        String refreshToken = refreshTokenRequest.getToken();

        // Giải mã refresh token để lấy username
        int userId = jwt.extractUserId(refreshToken);

        Account account = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));


        if (account.getUserName() == null) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Tạo key Redis theo username
        String refreshTokenKey = "REFRESH_TOKEN:" + account.getUserName();

        // Kiểm tra refresh token có tồn tại trong Redis không
        String storedToken = redisTemplate.opsForValue().get(refreshTokenKey);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Tạo access token mới
        var user = userRepository.findAccountByUserName(account.getUserName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var tokenPair = jwt.generateTokens(user);

        // Cập nhật refresh token mới vào Redis
        long expirationDays = 7; // Refresh token hết hạn sau 7 ngày
        redisTemplate.opsForValue().set(refreshTokenKey, tokenPair.refreshToken().token(), expirationDays, TimeUnit.DAYS);

        // Trả về access token mới
        return RefreshTokenResponse.builder()
                .accessToken(tokenPair.accessToken().token())
                .accessExpiryTime(tokenPair.accessToken().expiryDate())
                .build();
    }



    @Override
    public boolean register(SignUpRequest signUpRequest) {

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

        // Kiểm tra người dùng có tồn tại không
        var user = userRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.isActive()) {
            throw new AppException(ErrorCode.USER_ACTIVE);
        }

        // Lưu OTP vào Redis với thời gian hết hạn là 5 phút
        stringRedisTemplate.opsForValue().set("OTP:" + email, otpCode, 5, TimeUnit.MINUTES);

        // Gửi OTP qua email
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
            user.setEmail(email != null ? email : "none");
            user.setRole(role);
            user.setPassword("github");
            user.setActive(true);
            userRepository.save(user); // Lưu vào database
        }

        // Xóa Refresh Token cũ trên Redis (nếu có)
        String key = "REFRESH_TOKEN:" + user.getUserName();
        redisTemplate.delete(key);

        // Tạo mới bộ token (access token & refresh token)
        var tokenPair = jwt.generateTokens(user);

        // Lưu Refresh Token vào Redis với thời gian hết hạn 7 ngày (1 tuần)
        long expirationDays = 7; // 7 ngày
        redisTemplate.opsForValue().set(key, tokenPair.refreshToken().token(), expirationDays, TimeUnit.DAYS);

        // Trả về TokenResponse chứa access token và refresh token
        return TokenResponse.builder()
                .accessToken(tokenPair.accessToken().token())
                .refreshToken(tokenPair.refreshToken().token())
                .status(user.isActive())
                .build();
    }


    @Override
    public TokenResponse signupAndLoginWithFacebook(OAuth2User principal) {
        try {
            String email = principal.getAttribute("email");
            String firstName = principal.getAttribute("given_name");  // Lấy first name
            String lastName = principal.getAttribute("family_name");
            if (email == null ) {
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
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setPassword("google");
                user.setActive(true);
                userRepository.save(user);
            }

            // Xóa Refresh Token cũ trên Redis (nếu có)
            String key = "REFRESH_TOKEN:" + user.getUserName();
            redisTemplate.delete(key);

            // Tạo mới bộ token (access token & refresh token)
            var tokenPair = jwt.generateTokens(user);

            // Lưu Refresh Token vào Redis với thời gian hết hạn 7 ngày (1 tuần)
            long expirationDays = 7; // 7 ngày
            redisTemplate.opsForValue().set(key, tokenPair.refreshToken().token(), expirationDays, TimeUnit.DAYS);

            // Trả về TokenResponse chứa access token và refresh token
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
