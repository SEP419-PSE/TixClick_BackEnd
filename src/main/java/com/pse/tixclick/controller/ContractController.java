package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.ContractAndDocumentsDTO;
import com.pse.tixclick.payload.dto.ContractDTO;
import com.pse.tixclick.payload.entity.entity_enum.EVerificationStatus;
import com.pse.tixclick.payload.request.create.CreateContractAndDetailRequest;
import com.pse.tixclick.payload.request.create.CreateContractRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.ContractAndContractDetailResponse;
import com.pse.tixclick.service.ContractService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/contract")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContractController {
    ContractService contractService;


    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ContractAndDocumentsDTO>>> getAllContracts() {
        try {
            List<ContractAndDocumentsDTO> result = contractService.getAllContracts();
            return ResponseEntity.ok(
                    ApiResponse.<List<ContractAndDocumentsDTO>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Successfully fetched all contracts")
                            .result(result)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ContractAndDocumentsDTO>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Internal Server Error: " + e.getMessage())
                            .result(null)
                            .build());
        }
    }


    @PostMapping("/createContractAndContractDetail")
    public ResponseEntity<ApiResponse<CreateContractAndDetailRequest>> createContractAndContractDetail(@RequestParam("file") MultipartFile file){
        try {
            CreateContractAndDetailRequest result = contractService.createContractAndContractDetail(file);
            ApiResponse<CreateContractAndDetailRequest> response = ApiResponse.<CreateContractAndDetailRequest>builder()
                    .code(HttpStatus.OK.value())
                    .message("Contract and Contract Detail created successfully")
                    .result(result)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (AppException e) {
            ApiResponse<CreateContractAndDetailRequest> errorResponse = ApiResponse.<CreateContractAndDetailRequest>builder()
                     .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .result(null)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<CreateContractAndDetailRequest>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Internal Server Error: " + e.getMessage())
                            .result(null)
                            .build());
        }
    }
}
