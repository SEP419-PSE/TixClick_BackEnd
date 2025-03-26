package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
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


    @GetMapping("/get/{contractId}")
    public ResponseEntity<ApiResponse<List<ContractPaymentDTO>>> getContractPayment(@PathVariable int contractId) {
        try {
            List<ContractPaymentDTO> contractPaymentDTOs = contractPaymentService.getAllContractPaymentByContract(contractId);

            if (contractPaymentDTOs == null || contractPaymentDTOs.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.<List<ContractPaymentDTO>>builder()
                                .code(404)
                                .message("No Contract Payments found")
                                .result(Collections.emptyList()) // Trả về danh sách rỗng thay vì null
                                .build());
            }
            return ResponseEntity.ok(
                    ApiResponse.<List<ContractPaymentDTO>>builder()
                            .code(200)
                            .message("Contract Payment retrieved successfully")
                            .result(contractPaymentDTOs)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<List<ContractPaymentDTO>>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }
}
