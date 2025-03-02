package com.pse.tixclick.controller;

import com.pse.tixclick.payload.dto.BackgroundDTO;
import com.pse.tixclick.payload.dto.CompanyAccountDTO;
import com.pse.tixclick.payload.entity.company.CompanyAccount;
import com.pse.tixclick.payload.request.LoginRequest;
import com.pse.tixclick.payload.request.create.CreateCompanyAccountRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.TokenResponse;
import com.pse.tixclick.service.CompanyAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company-account")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class CompanyAccountController {
    @Autowired
    CompanyAccountService companyAccountService;

    @PostMapping("/login_with_company")
    public ResponseEntity<ApiResponse<TokenResponse>> loginWithCompanyAccount(@RequestBody LoginRequest loginRequest) {
        TokenResponse tokenResponse = companyAccountService.loginWithCompanyAccount(loginRequest);
        ApiResponse<TokenResponse> apiResponse = ApiResponse.<TokenResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Login successful")
                .result(tokenResponse)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<String>> createCompanyAccount(@RequestBody CreateCompanyAccountRequest createCompanyAccountRequest) {
        try {
            String companyAccount = companyAccountService.createCompanyAccount(createCompanyAccountRequest);;
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .code(200)
                            .message(companyAccount)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<String>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }
}
