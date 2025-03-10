package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.CompanyDTO;
import com.pse.tixclick.payload.request.create.CreateCompanyRequest;
import com.pse.tixclick.payload.request.create.CreateEventRequest;
import com.pse.tixclick.payload.request.update.UpdateCompanyRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.CreateCompanyResponse;
import com.pse.tixclick.payload.response.GetByCompanyResponse;
import com.pse.tixclick.payload.response.GetByCompanyWithVerificationResponse;
import com.pse.tixclick.service.CompanyService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/company")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class CompanyController {
    @Autowired
    private CompanyService companyService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CreateCompanyResponse>> createCompany(
            @ModelAttribute CreateCompanyRequest createCompanyRequest,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            CreateCompanyResponse companyDTO = companyService.createCompany(createCompanyRequest, file);
            return ResponseEntity.ok(
                    ApiResponse.<CreateCompanyResponse>builder()
                            .code(HttpStatus.OK.value())
                            .message("Company created successfully")
                            .result(companyDTO)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<CreateCompanyResponse>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<CreateCompanyResponse>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("File upload failed. Please try again.")
                            .result(null)
                            .build());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
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

    @PutMapping("/approve/{id}")
    public ResponseEntity<ApiResponse<String>> approveCompany(@PathVariable int id) {
        try {
            String message = companyService.approveCompany(id);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .code(HttpStatus.OK.value())
                            .message(message)
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
        }
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<ApiResponse<String>> rejectCompany(@PathVariable int id) {
        try {
            String message = companyService.rejectCompany(id);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .code(HttpStatus.OK.value())
                            .message(message)
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
        }
    }

    @PutMapping("/inactive/{id}")
    public ResponseEntity<ApiResponse<String>> inactiveCompany(@PathVariable int id) {
        try {
            String message = companyService.inactiveCompany(id);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .code(HttpStatus.OK.value())
                            .message(message)
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
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<GetByCompanyResponse>>> getAllCompany() {
        try {
            List<GetByCompanyResponse> companyDTOList = companyService.getAllCompany();
            return ResponseEntity.ok(
                    ApiResponse.<List<GetByCompanyResponse>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Get all company successfully")
                            .result(companyDTOList)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<List<GetByCompanyResponse>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<GetByCompanyResponse>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Internal server error")
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetByCompanyResponse>> getCompanyById(@PathVariable int id) {
        try {
            GetByCompanyResponse companyDTO = companyService.getCompanyById(id);
            return ResponseEntity.ok(
                    ApiResponse.<GetByCompanyResponse>builder()
                            .code(HttpStatus.OK.value())
                            .message("Get company by id successfully")
                            .result(companyDTO)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<GetByCompanyResponse>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<GetByCompanyResponse>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Internal server error")
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/manager")
    public ResponseEntity<ApiResponse<List<GetByCompanyWithVerificationResponse>>> getCompanysByManager() {
        try {
            List<GetByCompanyWithVerificationResponse> companyDTOList = companyService.getCompanysByManager();
            return ResponseEntity.ok(
                    ApiResponse.<List<GetByCompanyWithVerificationResponse>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Get all company successfully")
                            .result(companyDTOList)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<List<GetByCompanyWithVerificationResponse>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<GetByCompanyWithVerificationResponse>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Internal server error")
                            .result(null)
                            .build());
        }
    }
}
