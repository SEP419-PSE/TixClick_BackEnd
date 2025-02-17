package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.CompanyDTO;
import com.pse.tixclick.payload.request.CreateCompanyRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class CompanyController {
    @Autowired
    private CompanyService companyService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CompanyDTO>> createCompany(@RequestBody CreateCompanyRequest createCompanyRequest) {
        try {
            CompanyDTO companyDTO = companyService.createCompany(createCompanyRequest);
            return ResponseEntity.ok(
                    ApiResponse.<CompanyDTO>builder()
                            .code(HttpStatus.OK.value())
                            .message("Company created successfully")
                            .result(companyDTO)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.<CompanyDTO>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }
}
