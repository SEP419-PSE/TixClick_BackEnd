package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.CompanyVerificationDTO;
import com.pse.tixclick.payload.entity.company.CompanyAccount;
import com.pse.tixclick.payload.entity.company.CompanyVerification;
import com.pse.tixclick.payload.entity.entity_enum.EVerificationStatus;
import com.pse.tixclick.payload.entity.entity_enum.ECompanyStatus;
import com.pse.tixclick.payload.request.create.CreateCompanyVerificationRequest;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.CompanyAccountRepository;
import com.pse.tixclick.repository.CompanyRepository;
import com.pse.tixclick.repository.CompanyVerificationRepository;
import com.pse.tixclick.service.CompanyVerificationService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompanyVerificationServiceImpl implements CompanyVerificationService {
    CompanyVerificationRepository companyVerificationRepository;
    CompanyRepository companyRepository;
    AccountRepository accountRepository;
    ModelMapper modelMapper;
    CompanyAccountRepository companyAccountRepository;
    @Override
    public CompanyVerificationDTO createCompanyVerification(CreateCompanyVerificationRequest createCompanyVerificationRequest) {

        var account = accountRepository.findManagerWithLeastVerifications()
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));


        var company = companyRepository.findById(createCompanyVerificationRequest.getCompanyId())

                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        var companyVerification = new CompanyVerification();
        companyVerification.setCompany(company);
        companyVerification.setSubmitDate(createCompanyVerificationRequest.getSubmitDate());
        companyVerification.setStatus(createCompanyVerificationRequest.getStatus());
        companyVerification.setAccount(account);
        companyVerificationRepository.save(companyVerification);
        return  modelMapper.map(companyVerification, CompanyVerificationDTO.class);


    }

    @Override
    public CompanyVerificationDTO approveCompanyVerification(EVerificationStatus status, int companyVerificationId) {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        CompanyVerification companyVerification = companyVerificationRepository.
                findCompanyVerificationByCompanyVerificationIdAndAccount_UserName(companyVerificationId, username)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_VERIFICATION_NOT_FOUND));

        var company = companyRepository.findById(companyVerification.getCompany().getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));


        switch (status) {
            case APPROVED:

                CompanyAccount companyAccount = new CompanyAccount();
                companyAccount.setCompany(company);
                companyAccount.setAccount(company.getRepresentativeId());
                companyAccount.setUsername(company.getRepresentativeId().getUserName());
                companyAccount.setPassword(new BCryptPasswordEncoder(10).encode("123456"));
                companyAccountRepository.save(companyAccount);


                company.setStatus(ECompanyStatus.ACTIVE);
                break;

            case REJECTED:

                company.setStatus(ECompanyStatus.REJECTED);
                break;

            case PENDING:

                company.setStatus(ECompanyStatus.PENDING);
                break;

            default:

                company.setStatus(ECompanyStatus.INACTIVE);
                break;
        }

        companyRepository.save(company);

        // Cập nhật trạng thái xác minh
        companyVerification.setSubmitDate(LocalDateTime.now());
        companyVerification.setStatus(status);
        companyVerificationRepository.save(companyVerification);

        return modelMapper.map(companyVerification, CompanyVerificationDTO.class);
    }

    @Override
    public List<CompanyVerificationDTO> getCompanyVerificationsByManager() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        List<CompanyVerification> companyVerificationList = companyVerificationRepository.findCompanyVerificationsByAccount_UserName(username);
        if(companyVerificationList.isEmpty()) {
            throw new AppException(ErrorCode.COMPANY_VERIFICATION_NOT_FOUND);
        }
        return companyVerificationList.stream()
                .map(companyVerification -> modelMapper.map(companyVerification, CompanyVerificationDTO.class))
                .toList();

    }

    @Override
    public CompanyVerificationDTO resubmitCompanyVerification(int companyVerificationId) {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        CompanyVerification companyVerification = companyVerificationRepository
                .findCompanyVerificationByCompanyVerificationIdAndAccount_UserName(companyVerificationId, username)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_VERIFICATION_NOT_FOUND));
        if (companyVerification.getStatus() != EVerificationStatus.REJECTED) {
            throw new AppException(ErrorCode.COMPANY_VERIFICATION_NOT_REJECTED);
        }

        var account = accountRepository.findManagerWithLeastVerifications()
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        CompanyVerification resubmitCompanyVerification = new CompanyVerification();

        resubmitCompanyVerification.setCompany(companyVerification.getCompany());
        resubmitCompanyVerification.setAccount(account);
        resubmitCompanyVerification.setNote(companyVerification.getNote());
        resubmitCompanyVerification.setStatus(EVerificationStatus.PENDING);
        resubmitCompanyVerification.setSubmitDate(LocalDateTime.now());
        companyVerificationRepository.save(resubmitCompanyVerification);

        return modelMapper.map(resubmitCompanyVerification, CompanyVerificationDTO.class);
    }

}
