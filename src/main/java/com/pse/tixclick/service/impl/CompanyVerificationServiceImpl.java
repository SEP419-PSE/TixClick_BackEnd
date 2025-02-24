package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.CompanyVerificationDTO;
import com.pse.tixclick.payload.entity.company.CompanyVerification;
import com.pse.tixclick.payload.entity.entity_enum.CompanyVerificationStatus;
import com.pse.tixclick.payload.request.create.CreateCompanyVerificationRequest;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.CompanyRepository;
import com.pse.tixclick.repository.CompanyVerificationRepository;
import com.pse.tixclick.service.CompanyVerificationService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompanyVerificationServiceImpl implements CompanyVerificationService {
    CompanyVerificationRepository companyVerificationRepository;
    CompanyRepository companyRepository;
    AccountRepository accountRepository;
    ModelMapper modelMapper;
    @Override
    public CompanyVerificationDTO createCompanyVerification(CreateCompanyVerificationRequest createCompanyVerificationRequest) {




        var company = companyRepository.findById(createCompanyVerificationRequest.getCompanyId())

                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        var companyVerification = new CompanyVerification();
        companyVerification.setCompany(company);
        companyVerification.setSubmitDate(createCompanyVerificationRequest.getSubmitDate());
        companyVerification.setStatus(createCompanyVerificationRequest.getStatus());
        companyVerificationRepository.save(companyVerification);
        return  modelMapper.map(companyVerification, CompanyVerificationDTO.class);


    }
}
