package com.pse.tixclick.controller;

import com.pse.tixclick.payload.dto.ContractDetailDTO;
import com.pse.tixclick.payload.dto.ContractPaymentDTO;
import com.pse.tixclick.payload.dto.TicketPurchaseDTO;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.ContractPaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/pay/{id}")
    public ResponseEntity<ApiResponse<String>> payContract(@PathVariable int id) {
        try {
            String message = contractPaymentService.payContractPayment(id);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .code(200)
                            .message(message)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<String>builder()
                            .code(400)
                            .message(e.getMessage())
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
