package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.ContractDetailDTO;
import com.pse.tixclick.payload.dto.MyTicketDTO;
import com.pse.tixclick.payload.request.create.CreateContractDetailRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.QRCompanyResponse;
import com.pse.tixclick.service.ContractDetailService;
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
@RequestMapping("/contract-detail")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContractDetailController {
    @Autowired
    ContractDetailService contractDetailService;


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<List<ContractDetailDTO>>> createContractDetail(@RequestBody List<CreateContractDetailRequest> createContractDetailRequest, @RequestBody int contractId) {
        try {
            List<ContractDetailDTO> contractDetailDTOs = contractDetailService.createContractDetail(createContractDetailRequest, contractId);
            return ResponseEntity.ok(
                    ApiResponse.<List<ContractDetailDTO>>builder()
                            .code(200)
                            .message("Contract Detail created successfully")
                            .result(contractDetailDTOs)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<List<ContractDetailDTO>>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/get/{contractId}")
    public ResponseEntity<ApiResponse<List<ContractDetailDTO>>> getContractDetail(@PathVariable int contractId) {
        try {
            List<ContractDetailDTO> contractDetailDTOs = contractDetailService.getAllContractDetailByContract(contractId);

            if (contractDetailDTOs == null || contractDetailDTOs.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.<List<ContractDetailDTO>>builder()
                                .code(404)
                                .message("No Contract Details found")
                                .result(Collections.emptyList()) // Trả về danh sách rỗng thay vì null
                                .build());
            }
            return ResponseEntity.ok(
                    ApiResponse.<List<ContractDetailDTO>>builder()
                            .code(200)
                            .message("Contract Detail retrieved successfully")
                            .result(contractDetailDTOs)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<List<ContractDetailDTO>>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/qr/{contractId}")
    public ResponseEntity<ApiResponse<QRCompanyResponse>> getQRCompany(@PathVariable int contractId) {
        try {
            QRCompanyResponse myTicketDTO = contractDetailService.getQRCompany(contractId);
            return ResponseEntity.ok(
                    ApiResponse.<QRCompanyResponse>builder()
                            .code(HttpStatus.OK.value())
                            .message("QR Company retrieved successfully")
                            .result(myTicketDTO)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<QRCompanyResponse>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<QRCompanyResponse>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Internal Server Error: " + e.getMessage())
                            .result(null)
                            .build());
        }
    }

}
