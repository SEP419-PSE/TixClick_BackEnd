package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.CompanyDocumentDTO;
import com.pse.tixclick.payload.request.create.CreateCompanyDocumentRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.CompanyDocumentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/company-document")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompanyDocumentController {
    CompanyDocumentService companyDocumentService;

    @PostMapping(value = "/create")
    public ResponseEntity<ApiResponse<List<CompanyDocumentDTO>>> createCompanyDocument(
            @ModelAttribute CreateCompanyDocumentRequest request,
            @RequestParam("files") List<MultipartFile> files)

    {

        try {
            List<CompanyDocumentDTO> documentDTOs = companyDocumentService.createCompanyDocument(request, files);

            return ResponseEntity.ok(
                    ApiResponse.<List<CompanyDocumentDTO>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Documents uploaded successfully")
                            .result(documentDTOs)
                            .build()
            );

        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<List<CompanyDocumentDTO>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<CompanyDocumentDTO>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("An unexpected error occurred")
                            .result(null)
                            .build());
        }
    }
}
