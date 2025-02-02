package com.pse.tixclick.controller;

import com.nimbusds.jose.JOSEException;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.request.IntrospectRequest;
import com.pse.tixclick.payload.request.LoginRequest;
import com.pse.tixclick.payload.request.SignUpRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.RefreshTokenResponse;
import com.pse.tixclick.payload.response.TokenResponse;
import com.pse.tixclick.service.AuthenService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class AuthenController {
    @Autowired
    AuthenService authenService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody LoginRequest loginRequest) {
        TokenResponse tokenResponse = authenService.login(loginRequest);
        ApiResponse<TokenResponse> apiResponse = ApiResponse.<TokenResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Login successful")
                .result(tokenResponse)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody SignUpRequest signUpRequest, BindingResult bindingResult) {
        // Kiểm tra nếu có lỗi validation
        if (bindingResult.hasErrors()) {
            // Lấy tất cả thông báo lỗi
            String errorMessages = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message("Validation failed")
                    .result(errorMessages)
                    .build());
        }

        // Nếu không có lỗi validation, tiếp tục thực hiện logic đăng ký
        boolean success = authenService.register(signUpRequest);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Register successful")
                .result("Register successful")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
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
    public ResponseEntity<ApiResponse<Boolean>> verifyOTP(@RequestParam String email, @RequestParam String otpCode) {
        try {
            // Gọi service để xác thực mã OTP
            boolean isValid = authenService.verifyOTP(email, otpCode);

            // Kiểm tra nếu OTP không hợp lệ
            if (!isValid) {
                ApiResponse<Boolean> errorResponse = ApiResponse.<Boolean>builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message("OTP verification failed")
                        .result(false)
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);  // Trả về BAD_REQUEST
            }

            // Nếu OTP hợp lệ
            ApiResponse<Boolean> apiResponse = ApiResponse.<Boolean>builder()
                    .code(HttpStatus.OK.value())
                    .message("OTP verification successful")
                    .result(true)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);  // Trả về OK

        } catch (Exception e) {
            // Xử lý ngoại lệ nếu có lỗi xảy ra
            ApiResponse<Boolean> errorResponse = ApiResponse.<Boolean>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An error occurred during OTP verification")
                    .result(false)
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

}
