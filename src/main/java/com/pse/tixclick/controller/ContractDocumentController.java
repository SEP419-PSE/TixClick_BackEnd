package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.ContractDocumentDTO;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.ContractDocumentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/contract-document")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContractDocumentController {
    ContractDocumentService contractDocumentService;
    @PostMapping(value = "/upload")
    public ResponseEntity<ApiResponse<ContractDocumentDTO>> uploadContractDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("contractId") int contractId) {
        try {
            ContractDocumentDTO documentDTO = contractDocumentService.uploadContractDocument(file, contractId);
            return ResponseEntity.ok(
                    ApiResponse.<ContractDocumentDTO>builder()
                            .code(HttpStatus.OK.value())
                            .message("Contract document uploaded successfully")
                            .result(documentDTO)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<ContractDocumentDTO>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<ContractDocumentDTO>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("An unexpected error occurred")
                            .result(null)
                            .build());
        }
    }
}
