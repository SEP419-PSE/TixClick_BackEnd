package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.AccountDTO;
import com.pse.tixclick.payload.request.create.CreateAccountRequest;
import com.pse.tixclick.payload.request.update.UpdateAccountRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.SearchAccountResponse;
import com.pse.tixclick.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collections;
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
        try {
            // Gọi service để lấy thông tin tài khoản của người dùng hiện tại
            AccountDTO accountDTO = accountService.myProfile();

            // Tạo phản hồi API
            ApiResponse<AccountDTO> apiResponse = ApiResponse.<AccountDTO>builder()
                    .code(HttpStatus.OK.value())
                    .message("Profile retrieved successfully")
                    .result(accountDTO)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

        } catch (AppException e) {
            ApiResponse<AccountDTO> errorResponse = ApiResponse.<AccountDTO>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Lỗi từ service
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<AccountDTO> errorResponse = ApiResponse.<AccountDTO>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An unexpected error occurred.")
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping(value = "/update-profile", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<AccountDTO>> updateProfile(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String CCCD,
            @RequestParam(required = false) String MSSV,
            @RequestParam(required = false) String bankingCode,
            @RequestParam(required = false) String bankingName,
            @RequestParam(required = false) String ownerCard,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
            @RequestPart(required = false) MultipartFile avatarURL
    ) {
        try {
            // Tạo DTO từ request
            UpdateAccountRequest accountDTO = new UpdateAccountRequest();
            if (firstName != null && !firstName.isEmpty()) accountDTO.setFirstName(firstName);
            if (lastName != null && !lastName.isEmpty()) accountDTO.setLastName(lastName);
            if (CCCD != null && !CCCD.isEmpty()) accountDTO.setCCCD(CCCD);
            if (MSSV != null && !MSSV.isEmpty()) accountDTO.setMSSV(MSSV);
            if (bankingCode != null && !bankingCode.isEmpty()) accountDTO.setBankingCode(bankingCode);
            if (bankingName != null && !bankingName.isEmpty()) accountDTO.setBankingName(bankingName);
            if (ownerCard != null && !ownerCard.isEmpty()) accountDTO.setOwnerCard(ownerCard);
            if (email != null && !email.isEmpty()) accountDTO.setEmail(email);
            if (phone != null && !phone.isEmpty()) accountDTO.setPhone(phone);
            if (dob != null) accountDTO.setDob(dob);
            if (avatarURL != null && !avatarURL.isEmpty()) accountDTO.setAvatarURL(avatarURL);



            // Gọi service
            AccountDTO updatedAccount = accountService.updateProfile(accountDTO);

            // Trả về response chuẩn
            ApiResponse<AccountDTO> apiResponse = ApiResponse.<AccountDTO>builder()
                    .code(HttpStatus.OK.value())
                    .message("Profile updated successfully")
                    .result(updatedAccount)
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (AppException e) {
            ApiResponse<AccountDTO> errorResponse = ApiResponse.<AccountDTO>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            ApiResponse<AccountDTO> errorResponse = ApiResponse.<AccountDTO>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An unexpected error occurred.")
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
        } catch (Exception e) {
            ApiResponse<AccountDTO> errorResponse = ApiResponse.<AccountDTO>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An unexpected error occurred.")
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getAllAccounts() {
        try {
            // Gọi service để lấy danh sách tất cả tài khoản
            List<AccountDTO> accountDTOS = accountService.getAllAccount();

            // Tạo phản hồi API
            ApiResponse<List<AccountDTO>> apiResponse = ApiResponse.<List<AccountDTO>>builder()
                    .code(HttpStatus.OK.value())
                    .message("Accounts retrieved successfully")
                    .result(accountDTOS)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

        } catch (AppException e) {
            ApiResponse<List<AccountDTO>> errorResponse = ApiResponse.<List<AccountDTO>>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Lỗi từ service
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<List<AccountDTO>> errorResponse = ApiResponse.<List<AccountDTO>>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An unexpected error occurred.")
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/count-buyers")
    public ResponseEntity<ApiResponse<Integer>> countBuyers() {
        try {
            // Gọi service để lấy số lượng người mua
            int count = accountService.countTotalBuyers();

            // Tạo phản hồi API
            ApiResponse<Integer> apiResponse = ApiResponse.<Integer>builder()
                    .code(HttpStatus.OK.value())
                    .message("Buyers count retrieved successfully")
                    .result(count)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

        } catch (AppException e) {
            ApiResponse<Integer> errorResponse = ApiResponse.<Integer>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Lỗi từ service
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<Integer> errorResponse = ApiResponse.<Integer>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An unexpected error occurred.")
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/count-admins")
    public ResponseEntity<ApiResponse<Integer>> countAdmins() {
        try {
            // Gọi service để lấy số lượng quản trị viên
            int count = accountService.countTotalAdmins();

            // Tạo phản hồi API
            ApiResponse<Integer> apiResponse = ApiResponse.<Integer>builder()
                    .code(HttpStatus.OK.value())
                    .message("Admins count retrieved successfully")
                    .result(count)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

        } catch (AppException e) {
            ApiResponse<Integer> errorResponse = ApiResponse.<Integer>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Lỗi từ service
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<Integer> errorResponse = ApiResponse.<Integer>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An unexpected error occurred.")
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/count-organizers")
    public ResponseEntity<ApiResponse<Integer>> countOrganizers() {
        try {
            // Gọi service để lấy số lượng tổ chức viên
            int count = accountService.countTotalOrganizers();

            // Tạo phản hồi API
            ApiResponse<Integer> apiResponse = ApiResponse.<Integer>builder()
                    .code(HttpStatus.OK.value())
                    .message("Organizers count retrieved successfully")
                    .result(count)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

        } catch (AppException e) {
            ApiResponse<Integer> errorResponse = ApiResponse.<Integer>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Lỗi từ service
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<Integer> errorResponse = ApiResponse.<Integer>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An unexpected error occurred.")
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/count-managers")
    public ResponseEntity<ApiResponse<Integer>> countManagers() {
        try {
            // Gọi service để lấy số lượng quản lý viên
            int count = accountService.countTotalManagers();

            // Tạo phản hồi API
            ApiResponse<Integer> apiResponse = ApiResponse.<Integer>builder()
                    .code(HttpStatus.OK.value())
                    .message("Managers count retrieved successfully")
                    .result(count)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

        } catch (AppException e) {
            ApiResponse<Integer> errorResponse = ApiResponse.<Integer>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Lỗi từ service
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<Integer> errorResponse = ApiResponse.<Integer>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An unexpected error occurred.")
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/count-accounts")
    public ResponseEntity<ApiResponse<Integer>> countAccounts() {
        try {
            // Gọi service để lấy số lượng tài khoản
            int count = accountService.countTotalAccounts();

            // Tạo phản hồi API
            ApiResponse<Integer> apiResponse = ApiResponse.<Integer>builder()
                    .code(HttpStatus.OK.value())
                    .message("Accounts count retrieved successfully")
                    .result(count)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

        } catch (AppException e) {
            ApiResponse<Integer> errorResponse = ApiResponse.<Integer>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Lỗi từ service
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<Integer> errorResponse = ApiResponse.<Integer>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An unexpected error occurred.")
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/role-manager-admin")
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getAccountsByRoleManagerAndAdmin() {
        try {
            // Gọi service để lấy danh sách tài khoản theo vai trò quản lý viên và quản trị viên
            List<AccountDTO> accountDTOS = accountService.getAccountsByRoleManagerAndAdmin();

            // Tạo phản hồi API
            ApiResponse<List<AccountDTO>> apiResponse = ApiResponse.<List<AccountDTO>>builder()
                    .code(HttpStatus.OK.value())
                    .message("Accounts retrieved successfully")
                    .result(accountDTOS)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

        } catch (AppException e) {
            ApiResponse<List<AccountDTO>> errorResponse = ApiResponse.<List<AccountDTO>>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Lỗi từ service
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<List<AccountDTO>> errorResponse = ApiResponse.<List<AccountDTO>>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An unexpected error occurred.")
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/register-pin-code")
    public ResponseEntity<ApiResponse<String>> registerPinCode(@RequestParam String pinCode) {
        try {
            String pin = accountService.registerPinCode(pinCode);
            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .code(HttpStatus.OK.value())
                    .message("Pin code registered successfully")
                    .result(pin)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        } catch (AppException e) {
            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .result(null)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An unexpected error occurred.")
                    .result(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

    }

    @PostMapping("/login-with-pin-code")
    public ResponseEntity<ApiResponse<String>> loginWithPinCode(@RequestParam String pinCode) {
        try {
            String pin = accountService.loginWithPinCode(pinCode);
            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .code(HttpStatus.OK.value())
                    .message("Login with pin code successfully")
                    .result(pin)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        } catch (AppException e) {
            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .result(null)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An unexpected error occurred.")
                    .result(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/search-account")
    public ResponseEntity<ApiResponse<List<SearchAccountResponse>>> searchAccount(@RequestParam String email) {

        List<SearchAccountResponse> searchAccountResponse = accountService.searchAccount(email);
        if (searchAccountResponse.isEmpty()) {
            ApiResponse<List<SearchAccountResponse>> apiResponse = ApiResponse.<List<SearchAccountResponse>>builder()
                    .code(HttpStatus.OK.value())
                    .message("No accounts found")
                    .result(Collections.emptyList())
                    .build();
        }
        ApiResponse<List<SearchAccountResponse>> apiResponse = ApiResponse.<List<SearchAccountResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Account retrieved successfully")
                .result(searchAccountResponse)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);


    }
}
