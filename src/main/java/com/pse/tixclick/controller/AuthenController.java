package com.pse.tixclick.controller;

import com.nimbusds.jose.JOSEException;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.request.IntrospectRequest;
import com.pse.tixclick.payload.request.LoginRequest;
import com.pse.tixclick.payload.request.SignUpRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.GetToken;
import com.pse.tixclick.payload.response.RefreshTokenResponse;
import com.pse.tixclick.payload.response.TokenResponse;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.RoleRepository;
import com.pse.tixclick.service.AuthenService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class AuthenController {
    @Autowired
    AuthenService authenService;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    RoleRepository roleRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody LoginRequest loginRequest) {

        try
        {
            TokenResponse tokenResponse = authenService.login(loginRequest);
            ApiResponse<TokenResponse> apiResponse = ApiResponse.<TokenResponse>builder()
                    .code(HttpStatus.OK.value())
                    .message("Login successful")
                    .result(tokenResponse)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        } catch (AppException e) {
            ApiResponse<TokenResponse> errorResponse = ApiResponse.<TokenResponse>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getErrorCode().getMessage())
                    .result(null)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<TokenResponse> errorResponse = ApiResponse.<TokenResponse>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An error occurred during login")
                    .result(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }


    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody SignUpRequest signUpRequest, BindingResult bindingResult) {
        // Kiểm tra lỗi validation
        if (bindingResult.hasErrors()) {
            String errorMessages = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(errorMessages)
                    .result("Validation failed")
                    .build());
        }

        try {
            authenService.register(signUpRequest);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .code(HttpStatus.OK.value())
                    .message("Register successful")
                    .result("Register successful")
                    .build());
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.<String>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .result("Registration failed")
                    .build());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<Object>> refreshToken(@RequestBody IntrospectRequest refreshTokenRequest) {
        try {
            // Gọi service để làm mới refresh token
            RefreshTokenResponse tokenResponse = authenService.refreshToken(refreshTokenRequest);

            // Tạo phản hồi API khi thành công
            ApiResponse<Object> apiResponse = ApiResponse.<Object>builder()
                    .code(HttpStatus.OK.value())
                    .message("Refresh token successful")
                    .result(tokenResponse) // result là RefreshTokenResponse
                    .build();

            // Trả về response với mã HTTP 200
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        } catch (AppException | JOSEException | ParseException e) {
            // Nếu có lỗi, trả về phản hồi lỗi với mã trạng thái phù hợp
            ApiResponse<Object> errorResponse = ApiResponse.<Object>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message("Refresh token failed")
                    .result(e.getMessage()) // result là thông điệp lỗi dưới dạng String
                    .build();

            // Trả về phản hồi lỗi với mã BAD_REQUEST (400)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<Boolean>> introspect(@RequestBody IntrospectRequest introspectRequest) {
        // Gọi service để kiểm tra tính hợp lệ của token
        boolean isValid = authenService.introspect(introspectRequest).isValid();

        if (!isValid) {
            // Nếu không hợp lệ, trả về phản hồi với mã trạng thái BAD_REQUEST
            ApiResponse<Boolean> errorResponse = ApiResponse.<Boolean>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message("Introspect failed")
                    .result(false)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);  // Trả về BAD_REQUEST
        }

        // Nếu hợp lệ, trả về phản hồi với mã trạng thái OK
        ApiResponse<Boolean> apiResponse = ApiResponse.<Boolean>builder()
                .code(HttpStatus.OK.value())
                .message("Introspect successful")
                .result(isValid)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);  // Trả về OK
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<TokenResponse>> verifyOTP(@RequestParam String email, @RequestParam String otpCode) {
        try {
            // Gọi service để xác thực mã OTP
            TokenResponse isValid = authenService.verifyOTP(email, otpCode);



            // Nếu OTP hợp lệ
            ApiResponse<TokenResponse> apiResponse = ApiResponse.<TokenResponse>builder()
                    .code(HttpStatus.OK.value())
                    .message("OTP verification successful")
                    .result(isValid)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);  // Trả về OK

        } catch (AppException e) {
            // Nếu OTP không hợp lệ, trả về phản hồi với mã trạng thái BAD_REQUEST
            ApiResponse<TokenResponse> errorResponse = ApiResponse.<TokenResponse>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .result(null)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);  // Trả về BAD_REQUEST
        }
        catch (Exception e) {
            // Xử lý ngoại lệ nếu có lỗi xảy ra
            ApiResponse<TokenResponse> errorResponse = ApiResponse.<TokenResponse>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An error occurred during OTP verification")
                    .result(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);  // Trả về INTERNAL_SERVER_ERROR
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Boolean>> sendOTP(@RequestParam String email) {
        try {
            // Gọi service để tạo và gửi mã OTP
            authenService.createAndSendOTP(email);

            // Nếu thành công, trả về phản hồi với mã trạng thái OK
            ApiResponse<Boolean> apiResponse = ApiResponse.<Boolean>builder()
                    .code(HttpStatus.OK.value())
                    .message("OTP sent successfully")
                    .result(true)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);  // Trả về OK
        } catch (Exception e) {
            // Nếu có lỗi, trả về phản hồi với mã trạng thái BAD_REQUEST
            ApiResponse<Boolean> errorResponse = ApiResponse.<Boolean>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message("OTP sending failed: " + e.getMessage())
                    .result(false)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);  // Trả về BAD_REQUEST
        }
    }

    @GetMapping("/get-token")
    public ResponseEntity<ApiResponse<GetToken>> getToken() {
        try {
            // Gọi service để lấy thông tin từ token
            GetToken getToken = authenService.getToken();

            // Tạo phản hồi API
            ApiResponse<GetToken> apiResponse = ApiResponse.<GetToken>builder()
                    .code(HttpStatus.OK.value())
                    .message("Token information retrieved successfully")
                    .result(getToken)
                    .build();

            // Trả về response với mã HTTP 200 (OK)
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        } catch (Exception e) {
            // Nếu có lỗi, trả về phản hồi với mã trạng thái BAD_REQUEST
            ApiResponse<GetToken> errorResponse = ApiResponse.<GetToken>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message("Failed to retrieve token information")
                    .result(null)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


//    @GetMapping("/github/success")
//    public ResponseEntity<ApiResponse<TokenResponse>> signupWithGitHub(
//            @AuthenticationPrincipal OAuth2User principal
//            ) {
//
//        // Gọi service để đăng ký và đăng nhập
//        TokenResponse tokenResponse = authenService.signupAndLoginWithGitHub(principal);
//
//        return ResponseEntity.ok(new ApiResponse<>(200, "GitHub login successful", tokenResponse));
//    }

    @GetMapping("/google/success")
    public void googleLoginSuccess(
            @AuthenticationPrincipal OAuth2User principal,
            HttpServletResponse response
    ) throws IOException {
        TokenResponse tokenResponse = authenService.signupAndLoginWithGoogle(principal);

        String redirectUrl = UriComponentsBuilder
                .fromUriString("http://localhost:5173/success")
                .queryParam("accessToken", tokenResponse.getAccessToken())
                .queryParam("refreshToken", tokenResponse.getRefreshToken())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }



    @PostMapping("/login_with_manager_and_admin")
    public ResponseEntity<ApiResponse<TokenResponse>> loginWithManagerAndAdmin(@RequestBody LoginRequest loginRequest) {
        TokenResponse tokenResponse = authenService.loginWithManagerAndAdmin(loginRequest);
        ApiResponse<TokenResponse> apiResponse = ApiResponse.<TokenResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Login successful")
                .result(tokenResponse)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}
