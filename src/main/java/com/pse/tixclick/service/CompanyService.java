package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.CompanyDTO;
import com.pse.tixclick.payload.request.create.CreateCompanyRequest;
import com.pse.tixclick.payload.request.update.UpdateCompanyRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CompanyService {
    CompanyDTO createCompany(CreateCompanyRequest createCompanyRequest, MultipartFile file) throws IOException;

    CompanyDTO updateCompany(UpdateCompanyRequest updateCompanyRequest, int id);

    String approveCompany(String status);
}
