package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.AccountDTO;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/change-password-with-otp")
    public ResponseEntity<ApiResponse<Boolean>> changePasswordWithOtp(@RequestParam String email, @RequestParam String password) {
        try {
            // Gọi service để thay đổi mật khẩu
            boolean isChanged = accountService.changePasswordWithOtp(email, password);

            // Nếu thay đổi thành công, trả về phản hồi với mã trạng thái OK
            ApiResponse<Boolean> apiResponse = ApiResponse.<Boolean>builder()
                    .code(HttpStatus.OK.value())
                    .message("Password changed successfully")
                    .result(isChanged)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

        } catch (AppException e) {
            // Nếu có lỗi (ví dụ: người dùng không tồn tại), trả về phản hồi với mã trạng thái BAD_REQUEST
            ApiResponse<Boolean> errorResponse = ApiResponse.<Boolean>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Lỗi từ service
                    .result(false)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AccountDTO>> getProfile() {
        // Lấy thông tin tài khoản từ service
        AccountDTO accountDTO = accountService.myProfile();

        // Tạo phản hồi API
        ApiResponse<AccountDTO> apiResponse = ApiResponse.<AccountDTO>builder()
                .code(HttpStatus.OK.value())
                .message("Profile retrieved successfully")
                .result(accountDTO)
                .build();

        // Trả về thông tin tài khoản với mã HTTP 200
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}
