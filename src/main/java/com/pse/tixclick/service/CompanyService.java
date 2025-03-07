package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.CompanyDTO;
import com.pse.tixclick.payload.request.create.CreateCompanyRequest;
import com.pse.tixclick.payload.request.update.UpdateCompanyRequest;
import com.pse.tixclick.payload.response.GetByCompanyResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CompanyService {
    CompanyDTO createCompany(CreateCompanyRequest createCompanyRequest, MultipartFile file) throws IOException;

    CompanyDTO updateCompany(UpdateCompanyRequest updateCompanyRequest, int id);

    String approveCompany(int id);

    String rejectCompany(int id);

    String inactiveCompany(int id);

    List<GetByCompanyResponse> getAllCompany();

    GetByCompanyResponse getCompanyById(int id);
}
