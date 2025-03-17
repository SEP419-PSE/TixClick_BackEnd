package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.CompanyDocumentDTO;
import com.pse.tixclick.payload.request.create.CreateCompanyDocumentRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CompanyDocumentService {
    List<CompanyDocumentDTO> createCompanyDocument(CreateCompanyDocumentRequest createCompanyDocumentRequest, MultipartFile files) throws IOException;

    boolean deleteCompanyDocument(int companyDocumentId);
}
