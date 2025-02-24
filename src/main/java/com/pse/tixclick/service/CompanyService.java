package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.CompanyDTO;
import com.pse.tixclick.payload.request.create.CreateCompanyRequest;
import com.pse.tixclick.payload.request.update.UpdateCompanyRequest;

public interface CompanyService {
    CompanyDTO createCompany(CreateCompanyRequest createCompanyRequest);

    CompanyDTO updateCompany(UpdateCompanyRequest updateCompanyRequest, int id);

    String approveCompany(String status);
}
