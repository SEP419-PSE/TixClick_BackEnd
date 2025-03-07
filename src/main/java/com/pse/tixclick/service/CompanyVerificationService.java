package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.CompanyVerificationDTO;
import com.pse.tixclick.payload.entity.entity_enum.EVerificationStatus;
import com.pse.tixclick.payload.request.create.CreateCompanyVerificationRequest;
import jakarta.mail.MessagingException;

import java.util.List;

public interface CompanyVerificationService {

    CompanyVerificationDTO createCompanyVerification(CreateCompanyVerificationRequest createCompanyVerificationRequest);

    CompanyVerificationDTO approveCompanyVerification(EVerificationStatus status, int companyVerificationId) throws MessagingException;

    List<CompanyVerificationDTO> getCompanyVerificationsByManager();

    CompanyVerificationDTO resubmitCompanyVerification(int companyVerificationId);
}
