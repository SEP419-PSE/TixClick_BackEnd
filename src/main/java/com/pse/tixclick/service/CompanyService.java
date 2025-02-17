package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.CompanyDTO;
import com.pse.tixclick.payload.request.CreateCompanyRequest;

public interface CompanyService {
    CompanyDTO createCompany(CreateCompanyRequest createCompanyRequest);
}
