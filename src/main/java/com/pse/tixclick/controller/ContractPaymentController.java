package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.ContractAndContractPaymentDTO;
import com.pse.tixclick.payload.dto.ContractPaymentDTO;
import com.pse.tixclick.payload.request.ContractPaymentRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.ContractPaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/contract-payment")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContractPaymentController {
    @Autowired
    ContractPaymentService contractPaymentService;

    @GetMapping("/pay")
    public ResponseEntity<ApiResponse<ContractPaymentRequest>> payContractPayment(
            @RequestParam String transactionCode,
            @RequestParam int paymentId) {
        try {
            ContractPaymentRequest response = contractPaymentService.getContractPayment(transactionCode, paymentId);
            return ResponseEntity.ok(
                    ApiResponse.<ContractPaymentRequest>builder()
                            .code(HttpStatus.OK.value())
                            .message("Contract Payment processed successfully")
                            .result(response)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<ContractPaymentRequest>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<ContractPaymentRequest>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Internal server error")
                            .result(null)
                            .build());
        }
    }


    @GetMapping("/get")
    public ResponseEntity<ApiResponse<List<ContractAndContractPaymentDTO>>> getContractPayment() {
        try {
            List<ContractAndContractPaymentDTO> contractPaymentDTOs = contractPaymentService.getAllContractPaymentByContract();

            // Luôn trả về 200 OK, kể cả khi danh sách rỗng
            return ResponseEntity.ok(
                    ApiResponse.<List<ContractAndContractPaymentDTO>>builder()
                            .code(200)
                            .message(contractPaymentDTOs.isEmpty()
                                    ? "No Contract Payments found"
                                    : "Contract Payment retrieved successfully")
                            .result(contractPaymentDTOs)
                            .build()
            );
        } catch (AppException e) {
            // Nếu là lỗi quyền hoặc business logic => vẫn có thể trả về 403 hoặc 400 tùy theo context
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<List<ContractAndContractPaymentDTO>>builder()
                            .code(200)
                            .message(e.getErrorCode().getMessage())
                            .result(Collections.emptyList())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.<List<ContractAndContractPaymentDTO>>builder()
                            .code(500)
                            .message("Internal Server Error: " + e.getMessage())
                            .result(Collections.emptyList())
                            .build());
        }
    }

}
