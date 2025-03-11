package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.ContractDTO;
import com.pse.tixclick.payload.request.create.CreateContractRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.ContractService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contract")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContractController {
    ContractService contractService;
    @PostMapping
    public ResponseEntity<ApiResponse<ContractDTO>> createContract(
            @RequestBody CreateContractRequest request) {
        try {
            ContractDTO result = contractService.createContract(request);
            return ResponseEntity.ok(
                    ApiResponse.<ContractDTO>builder()
                            .code(HttpStatus.OK.value())
                            .message("Contract created successfully")
                            .result(result)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<ContractDTO>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<ContractDTO>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Internal Server Error: " + e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ContractDTO>>> getAllContracts() {
        try {
            List<ContractDTO> result = contractService.getAllContracts();
            return ResponseEntity.ok(
                    ApiResponse.<List<ContractDTO>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Successfully fetched all contracts")
                            .result(result)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ContractDTO>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Internal Server Error: " + e.getMessage())
                            .result(null)
                            .build());
        }
    }
}
