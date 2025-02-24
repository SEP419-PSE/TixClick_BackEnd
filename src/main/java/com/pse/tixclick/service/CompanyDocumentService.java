package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.CompanyDocumentDTO;
import com.pse.tixclick.payload.request.create.CreateCompanyDocumentRequest;

public interface CompanyDocumentService {
    CompanyDocumentDTO createCompanyDocument(CreateCompanyDocumentRequest createCompanyDocumentRequest);
}
