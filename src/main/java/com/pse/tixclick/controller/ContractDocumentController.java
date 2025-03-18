package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.ContractDocumentDTO;
import com.pse.tixclick.payload.entity.company.ContractDocument;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.ContractDocumentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.security.PrivateKey;
import java.util.List;

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

    @PostMapping("/sign-and-upload/{contractDocumentId}")
    public String signAndUploadPdf(@PathVariable int contractDocumentId) throws Exception {
        return contractDocumentService.signPdf(contractDocumentId);
    }

    @GetMapping(value = "/get/{contractId}")
    public ResponseEntity<ApiResponse<ContractDocumentDTO>> getContractDocument(
            @RequestParam("contractId") int contractId) {
        try {
            ContractDocumentDTO documentDTO = contractDocumentService.getContractDocument(contractId);
            return ResponseEntity.ok(
                    ApiResponse.<ContractDocumentDTO>builder()
                            .code(HttpStatus.OK.value())
                            .message("Contract document retrieved successfully")
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

    @GetMapping(value = "/all_by_company/{companyId}")
    public ResponseEntity<ApiResponse<List<ContractDocumentDTO>>> getContractDocumentsByCompany(
            @RequestParam("companyId") int companyId) {
        try {
            List<ContractDocumentDTO> documentDTOs = contractDocumentService.getContractDocumentsByCompany(companyId);
            return ResponseEntity.ok(
                    ApiResponse.<List<ContractDocumentDTO>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Contract documents retrieved successfully")
                            .result(documentDTOs)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<List<ContractDocumentDTO>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ContractDocumentDTO>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("An unexpected error occurred")
                            .result(null)
                            .build());
        }
    }

    @DeleteMapping(value = "/delete/{contractDocumentId}")
    public ResponseEntity<ApiResponse<String>> deleteContractDocument(
            @RequestParam("contractDocumentId") int contractDocumentId) {
        try {
            contractDocumentService.deleteContractDocument(contractDocumentId);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .code(HttpStatus.OK.value())
                            .message("Contract document deleted successfully")
                            .result(null)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<String>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<String>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("An unexpected error occurred")
                            .result(null)
                            .build());
        }
    }

    @GetMapping(value = "/all")
    public ResponseEntity<ApiResponse<List<ContractDocumentDTO>>> getAllContractDocuments() {
        try {
            List<ContractDocumentDTO> documentDTOs = contractDocumentService.getAllContractDocuments();
            return ResponseEntity.ok(
                    ApiResponse.<List<ContractDocumentDTO>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Contract documents retrieved successfully")
                            .result(documentDTOs)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<List<ContractDocumentDTO>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ContractDocumentDTO>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("An unexpected error occurred")
                            .result(null)
                            .build());
        }
    }

    @GetMapping(value = "/all_by_event/{eventId}")
    public ResponseEntity<ApiResponse<List<ContractDocumentDTO>>> getContractDocumentsByEvent(
            @RequestParam("eventId") int eventId) {
        try {
            List<ContractDocumentDTO> documentDTOs = contractDocumentService.getContractDocumentsByEvent(eventId);
            return ResponseEntity.ok(
                    ApiResponse.<List<ContractDocumentDTO>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Contract documents retrieved successfully")
                            .result(documentDTOs)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<List<ContractDocumentDTO>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ContractDocumentDTO>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("An unexpected error occurred")
                            .result(null)
                            .build());
        }
    }

    @GetMapping(value = "/all_by_contract/{contractId}")
    public ResponseEntity<ApiResponse<List<ContractDocumentDTO>>> getContractDocumentsByContract(
            @RequestParam("contractId") int contractId) {
        try {
            List<ContractDocumentDTO> documentDTOs = contractDocumentService.getContractDocumentsByContract(contractId);
            return ResponseEntity.ok(
                    ApiResponse.<List<ContractDocumentDTO>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Contract documents retrieved successfully")
                            .result(documentDTOs)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<List<ContractDocumentDTO>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ContractDocumentDTO>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("An unexpected error occurred")
                            .result(null)
                            .build());
        }
    }
}
