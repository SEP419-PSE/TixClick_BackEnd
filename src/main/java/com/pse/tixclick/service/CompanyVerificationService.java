package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.CompanyVerificationDTO;
import com.pse.tixclick.payload.entity.company.CompanyVerification;
import com.pse.tixclick.payload.entity.entity_enum.CompanyVerificationStatus;
import com.pse.tixclick.payload.request.create.CreateCompanyVerificationRequest;

import java.util.List;

public interface CompanyVerificationService {

    CompanyVerificationDTO createCompanyVerification(CreateCompanyVerificationRequest createCompanyVerificationRequest);

    CompanyVerificationDTO approveCompanyVerification(CompanyVerificationStatus status, int companyVerificationId);

    List<CompanyVerificationDTO> getCompanyVerificationsByManager();

    CompanyVerificationDTO resubmitCompanyVerification(int companyVerificationId);
}
