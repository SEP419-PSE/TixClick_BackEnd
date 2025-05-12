package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.CompanyDTO;
import com.pse.tixclick.payload.dto.CompanyDocumentDTO;
import com.pse.tixclick.payload.request.create.CreateCompanyRequest;
import com.pse.tixclick.payload.request.update.UpdateCompanyRequest;
import com.pse.tixclick.payload.response.*;
import jakarta.mail.MessagingException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CompanyService {



    List<GetByCompanyResponse> getAllCompany();

    GetByCompanyResponse getCompanyById(int id);

    List<CompanyDTO> getCompanyByAccountId();


    List<GetByCompanyWithVerificationResponse> getCompanysByManager();

    CompanyAndDocumentResponse createCompanyAndDocument(CreateCompanyRequest createCompanyRequest, MultipartFile logoURL, List<MultipartFile> companyDocument) throws IOException, MessagingException;
    CompanyDTO isAccountHaveCompany();

    GetTransactionPaymenByCompanyIdResponse getTransactionPaymentContractByCompanyId(int companyId);

    List<MyCompanyResponse> getCompanysByUserName(String userName);

    CompanyDTO getCompanyByEventId(int eventId);

    CreateCompanyResponse updateCompany(int companyId, CreateCompanyRequest updateRequest, MultipartFile file, List<MultipartFile> fileDocument) throws IOException, MessagingException;

    List<CompanyDocumentDTO> getDocumentByCompanyId(int companyId);
}
