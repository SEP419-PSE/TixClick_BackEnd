package com.pse.tixclick.service.impl;

import com.cloudinary.Cloudinary;
import com.pse.tixclick.cloudinary.CloudinaryService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.CompanyDTO;
import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.company.CompanyAccount;
import com.pse.tixclick.payload.entity.company.Member;
import com.pse.tixclick.payload.entity.entity_enum.CompanyVerificationStatus;
import com.pse.tixclick.payload.entity.entity_enum.ECompanyStatus;
import com.pse.tixclick.payload.entity.entity_enum.EStatus;
import com.pse.tixclick.payload.entity.entity_enum.ESubRole;
import com.pse.tixclick.payload.request.create.CreateCompanyRequest;
import com.pse.tixclick.payload.request.create.CreateCompanyVerificationRequest;
import com.pse.tixclick.payload.request.update.UpdateCompanyRequest;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.CompanyAccountRepository;
import com.pse.tixclick.repository.CompanyRepository;
import com.pse.tixclick.repository.MemberRepository;
import com.pse.tixclick.service.CompanyService;
import com.pse.tixclick.service.CompanyVerificationService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompanyServiceImpl implements CompanyService {
    CompanyRepository companyRepository;
    AccountRepository accountRepository;
    MemberRepository memberRepository;
    CompanyAccountRepository companyAccountRepository;
    ModelMapper modelMapper;
    CompanyVerificationService companyVerificationService;
    CloudinaryService cloudinary;
    @Override
    public CompanyDTO createCompany(CreateCompanyRequest createCompanyRequest, MultipartFile file) throws IOException {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        var account = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String logoURL = cloudinary.uploadImageToCloudinary(file);


        Company company = new Company();
        company.setCompanyName(createCompanyRequest.getCompanyName());
        company.setDescription(createCompanyRequest.getDescription());
        company.setRepresentativeId(account);
        company.setCodeTax(createCompanyRequest.getCodeTax());
        company.setBankingCode(createCompanyRequest.getBankingCode());
        company.setBankingName(createCompanyRequest.getBankingName());
        company.setNationalId(createCompanyRequest.getNationalId());
        company.setLogoURL(logoURL);
        company.setStatus(ECompanyStatus.INACTIVE);
        companyRepository.save(company);

        String generatedUsername = company.getCompanyName().toLowerCase().replaceAll("\\s+", "") + (int)(Math.random() * 1000);


        CompanyAccount companyAccount = new CompanyAccount();
        companyAccount.setCompany(company);
        companyAccount.setAccount(account);
        companyAccount.setUsername(generatedUsername);
        companyAccount.setPassword(new BCryptPasswordEncoder(10).encode("123456"));
        companyAccountRepository.save(companyAccount);

        Member member = new Member();
        member.setCompany(company);
        member.setSubRole(ESubRole.OWNER);
        member.setAccount(account);
        member.setStatus(EStatus.ACTIVE);
        memberRepository.save(member);

        CreateCompanyVerificationRequest createCompanyVerificationRequest = new CreateCompanyVerificationRequest();
        createCompanyVerificationRequest.setCompanyId(company.getCompanyId());
        createCompanyVerificationRequest.setStatus(CompanyVerificationStatus.PENDING);


        companyVerificationService.createCompanyVerification(createCompanyVerificationRequest);

        return modelMapper.map(company, CompanyDTO.class);

    }

    @Override
    public CompanyDTO updateCompany(UpdateCompanyRequest updateCompanyRequest, int id) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        Company company = companyRepository.findCompanyByCompanyIdAndRepresentativeId_UserName(id, name)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));

        company.setCompanyName(updateCompanyRequest.getCompanyName());
        company.setDescription(updateCompanyRequest.getDescription());
        company.setCodeTax(updateCompanyRequest.getCodeTax());
        company.setBankingCode(updateCompanyRequest.getBankingCode());
        company.setBankingName(updateCompanyRequest.getBankingName());
        company.setNationalId(updateCompanyRequest.getNationalId());
        company.setLogoURL(updateCompanyRequest.getLogoURL());
        companyRepository.save(company);

        return modelMapper.map(company, CompanyDTO.class);
    }

    @Override
    public String approveCompany(String status) {
        return null;
    }
}
