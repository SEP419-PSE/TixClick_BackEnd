package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.AccountDTO;
import com.pse.tixclick.payload.request.create.CreateAccountRequest;
import com.pse.tixclick.payload.request.update.UpdateAccountRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RestController
@RequestMapping("/account")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/change-password-with-otp")
    public ResponseEntity<ApiResponse<Boolean>> changePasswordWithOtp(
            @RequestParam String email,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            // Call the service to change the password
            boolean isChanged = accountService.changePasswordWithOtp(email, newPassword, oldPassword);

            // If password change is successful, return a success response
            ApiResponse<Boolean> apiResponse = ApiResponse.<Boolean>builder()
                    .code(HttpStatus.OK.value())
                    .message("Password changed successfully")
                    .result(isChanged)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

        } catch (AppException e) {
            // If an error occurs (e.g., user does not exist or passwords don't match), return a BAD_REQUEST response
            ApiResponse<Boolean> errorResponse = ApiResponse.<Boolean>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Error message from service
                    .result(false)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            // General exception handler for unforeseen errors
            ApiResponse<Boolean> errorResponse = ApiResponse.<Boolean>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An unexpected error occurred.")
                    .result(false)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @GetMapping("/my-profile")
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

    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse<AccountDTO>> updateProfile(@RequestBody UpdateAccountRequest accountDTO) {
        try {
            // Gọi service để cập nhật thông tin tài khoản
            AccountDTO updatedAccount = accountService.updateProfile(accountDTO);

            // Tạo phản hồi API
            ApiResponse<AccountDTO> apiResponse = ApiResponse.<AccountDTO>builder()
                    .code(HttpStatus.OK.value())
                    .message("Profile updated successfully")
                    .result(updatedAccount)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

        } catch (AppException e) {
            ApiResponse<AccountDTO> errorResponse = ApiResponse.<AccountDTO>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Lỗi từ service
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/create-account")
    public ResponseEntity<ApiResponse<AccountDTO>> createAccount(@RequestBody CreateAccountRequest accountDTO) {
        try {
            // Gọi service để tạo tài khoản
            AccountDTO createdAccount = accountService.createAccount(accountDTO);

            // Tạo phản hồi API
            ApiResponse<AccountDTO> apiResponse = ApiResponse.<AccountDTO>builder()
                    .code(HttpStatus.OK.value())
                    .message("Account created successfully")
                    .result(createdAccount)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

        } catch (AppException e) {
            ApiResponse<AccountDTO> errorResponse = ApiResponse.<AccountDTO>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Lỗi từ service
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getAllAccounts() {
        List<AccountDTO> accountDTOS = accountService.getAllAccount();

        ApiResponse<List<AccountDTO>> apiResponse = ApiResponse.<List<AccountDTO>>builder()
                .code(HttpStatus.OK.value())
                .message("All accounts retrieved successfully")
                .result(accountDTOS)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/count-buyers")
    public ResponseEntity<ApiResponse<Integer>> countBuyers() {
        int count = accountService.countTotalBuyers();

        ApiResponse<Integer> apiResponse = ApiResponse.<Integer>builder()
                .code(HttpStatus.OK.value())
                .message("Buyers count retrieved successfully")
                .result(count)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/count-admins")
    public ResponseEntity<ApiResponse<Integer>> countAdmins() {
        int count = accountService.countTotalAdmins();

        ApiResponse<Integer> apiResponse = ApiResponse.<Integer>builder()
                .code(HttpStatus.OK.value())
                .message("Admins count retrieved successfully")
                .result(count)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/count-organizers")
    public ResponseEntity<ApiResponse<Integer>> countOrganizers() {
        int count = accountService.countTotalOrganizers();

        ApiResponse<Integer> apiResponse = ApiResponse.<Integer>builder()
                .code(HttpStatus.OK.value())
                .message("Organizers count retrieved successfully")
                .result(count)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/count-managers")
    public ResponseEntity<ApiResponse<Integer>> countManagers() {
        int count = accountService.countTotalManagers();

        ApiResponse<Integer> apiResponse = ApiResponse.<Integer>builder()
                .code(HttpStatus.OK.value())
                .message("Managers count retrieved successfully")
                .result(count)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/count-accounts")
    public ResponseEntity<ApiResponse<Integer>> countAccounts() {
        int count = accountService.countTotalAccounts();

        ApiResponse<Integer> apiResponse = ApiResponse.<Integer>builder()
                .code(HttpStatus.OK.value())
                .message("Accounts count retrieved successfully")
                .result(count)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/role-manager-admin")
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getAccountsByRoleManagerAndAdmin() {
        List<AccountDTO> accountDTOS = accountService.getAccountsByRoleManagerAndAdmin();

        ApiResponse<List<AccountDTO>> apiResponse = ApiResponse.<List<AccountDTO>>builder()
                .code(HttpStatus.OK.value())
                .message("Accounts retrieved successfully")
                .result(accountDTOS)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}
