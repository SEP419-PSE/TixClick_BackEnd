package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.CompanyVerificationDTO;
import com.pse.tixclick.payload.entity.entity_enum.CompanyVerificationStatus;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.CompanyVerificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company-verification")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompanyVerificationController {
    CompanyVerificationService companyVerificationService;

    @PatchMapping("/{companyVerificationId}/approve")
    public ResponseEntity<ApiResponse<CompanyVerificationDTO>> approveCompanyVerification(
            @PathVariable int companyVerificationId,
            @RequestParam CompanyVerificationStatus status) {
        try {
            CompanyVerificationDTO result = companyVerificationService.approveCompanyVerification(status, companyVerificationId);

            return ResponseEntity.ok(
                    ApiResponse.<CompanyVerificationDTO>builder()
                            .code(HttpStatus.OK.value())
                            .message("Company verification updated successfully")
                            .result(result)
                            .build()
            );

        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<CompanyVerificationDTO>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<CompanyVerificationDTO>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("An unexpected error occurred")
                            .result(null)
                            .build());
        }
    }

    @PatchMapping("/{companyVerificationId}/resubmit")
    public ResponseEntity<ApiResponse<CompanyVerificationDTO>> resubmitCompanyVerification(
            @PathVariable int companyVerificationId) {
        try {
            CompanyVerificationDTO result = companyVerificationService.resubmitCompanyVerification(companyVerificationId);

            return ResponseEntity.ok(
                    ApiResponse.<CompanyVerificationDTO>builder()
                            .code(HttpStatus.OK.value())
                            .message("Company verification updated successfully")
                            .result(result)
                            .build()
            );

        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<CompanyVerificationDTO>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<CompanyVerificationDTO>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("An unexpected error occurred")
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/manager")
    public ResponseEntity<ApiResponse<List<CompanyVerificationDTO>>> getCompanyVerificationsByManager() {
        try {
            List<CompanyVerificationDTO> result = companyVerificationService.getCompanyVerificationsByManager();

            return ResponseEntity.ok(
                    ApiResponse.<List<CompanyVerificationDTO>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Company verifications retrieved successfully")
                            .result(result)
                            .build()
            );

        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<List<CompanyVerificationDTO>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<CompanyVerificationDTO>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("An unexpected error occurred")
                            .result(null)
                            .build());
        }
    }
}
