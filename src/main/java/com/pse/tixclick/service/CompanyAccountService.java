package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.CompanyAccountDTO;
import com.pse.tixclick.payload.request.LoginRequest;
import com.pse.tixclick.payload.request.create.CreateCompanyAccountRequest;
import com.pse.tixclick.payload.response.TokenResponse;

public interface CompanyAccountService {
    String createCompanyAccount (CreateCompanyAccountRequest createCompanyAccountRequest);

    TokenResponse loginWithCompanyAccount(LoginRequest loginRequest);
}
