package com.pse.tixclick.controller;

import com.pse.tixclick.payload.dto.ContractDetailDTO;
import com.pse.tixclick.payload.request.create.CreateContractDetailRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.ContractDetailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
