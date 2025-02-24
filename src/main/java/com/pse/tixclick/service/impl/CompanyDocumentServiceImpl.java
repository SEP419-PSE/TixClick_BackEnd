package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.CompanyDocumentDTO;
import com.pse.tixclick.payload.entity.company.CompanyDocuments;
import com.pse.tixclick.payload.request.create.CreateCompanyDocumentRequest;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.CompanyDocumentRepository;
import com.pse.tixclick.repository.CompanyRepository;
import com.pse.tixclick.service.CompanyDocumentService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompanyDocumentServiceImpl implements CompanyDocumentService {
    CompanyDocumentRepository companyDocumentRepository;
    AccountRepository accountRepository;
    CompanyRepository companyRepository;
    ModelMapper modelMapper;
    @Override
    public CompanyDocumentDTO createCompanyDocument(CreateCompanyDocumentRequest createCompanyDocumentRequest) {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        var company = companyRepository.findCompanyByCompanyIdAndRepresentativeId_UserName(createCompanyDocumentRequest.getCompanyId(), username)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        var companyDocument = new CompanyDocuments();
        companyDocument.setCompany(company);
        companyDocument.setFileName(createCompanyDocumentRequest.getFileName());
        companyDocument.setFileURL(createCompanyDocumentRequest.getFileURL());
        companyDocument.setFileType(createCompanyDocumentRequest.getFileType());
        companyDocument.setUploadDate(createCompanyDocumentRequest.getUploadDate());

        companyDocumentRepository.save(companyDocument);

        return modelMapper.map(companyDocument, CompanyDocumentDTO.class);

    }
}
