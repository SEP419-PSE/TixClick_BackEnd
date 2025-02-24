package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.CompanyVerificationDTO;
import com.pse.tixclick.payload.entity.company.CompanyVerification;
import com.pse.tixclick.payload.request.create.CreateCompanyVerificationRequest;

public interface CompanyVerificationService {

    CompanyVerificationDTO createCompanyVerification(CreateCompanyVerificationRequest createCompanyVerificationRequest);
}
