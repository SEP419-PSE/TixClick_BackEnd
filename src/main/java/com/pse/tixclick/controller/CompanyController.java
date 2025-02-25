package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.CompanyDTO;
import com.pse.tixclick.payload.request.create.CreateCompanyRequest;
import com.pse.tixclick.payload.request.create.CreateEventRequest;
import com.pse.tixclick.payload.request.update.UpdateCompanyRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/company")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class CompanyController {
    @Autowired
    private CompanyService companyService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CompanyDTO>> createCompany(
            @ModelAttribute CreateCompanyRequest createCompanyRequest,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            CompanyDTO companyDTO = companyService.createCompany(createCompanyRequest, file);
            return ResponseEntity.ok(
                    ApiResponse.<CompanyDTO>builder()
                            .code(HttpStatus.OK.value())
                            .message("Company created successfully")
                            .result(companyDTO)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<CompanyDTO>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<CompanyDTO>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("File upload failed. Please try again.")
                            .result(null)
                            .build());
        }
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<CompanyDTO>> updateCompany(@RequestBody UpdateCompanyRequest updateCompanyRequest, @PathVariable int id) {
        try {
            CompanyDTO companyDTO = companyService.updateCompany(updateCompanyRequest, id);
            return ResponseEntity.ok(
                    ApiResponse.<CompanyDTO>builder()
                            .code(HttpStatus.OK.value())
                            .message("Company updated successfully")
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
