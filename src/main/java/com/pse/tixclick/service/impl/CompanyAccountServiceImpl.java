package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.jwt.Jwt;
import com.pse.tixclick.payload.dto.CompanyAccountDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.company.CompanyAccount;
import com.pse.tixclick.payload.request.LoginRequest;
import com.pse.tixclick.payload.request.SignUpRequest;
import com.pse.tixclick.payload.request.create.CreateCompanyAccountRequest;
import com.pse.tixclick.payload.response.TokenResponse;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.CompanyAccountRepository;
import com.pse.tixclick.repository.CompanyRepository;
import com.pse.tixclick.service.CompanyAccountService;
import com.pse.tixclick.utils.AppUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class CompanyAccountServiceImpl implements CompanyAccountService {
    @Autowired
    AppUtils appUtils;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    Jwt jwt;

    @Autowired
    CompanyAccountRepository companyAccountRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    CompanyRepository company;


    @Override
    public String createCompanyAccount(CreateCompanyAccountRequest createCompanyAccountRequest) {
        if (companyAccountRepository.findByUserName(createCompanyAccountRequest.getUserName()).isPresent()) {
            throw new AppException(ErrorCode.COMPANY_USERNAME_EXISTED);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        Account account = accountRepository
                .findById(createCompanyAccountRequest.getAccountId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        Company company = companyRepository
                .findById(createCompanyAccountRequest.getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        if(companyAccountRepository.findByUserName(createCompanyAccountRequest.getUserName()).isPresent()){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        CompanyAccount companyAccount = new CompanyAccount();
        companyAccount.setAccount(account);
        companyAccount.setCompany(company);
        companyAccount.setUsername(createCompanyAccountRequest.getUserName());
        companyAccount.setPassword(passwordEncoder.encode(createCompanyAccountRequest.getPassword()));

        companyAccountRepository.save(companyAccount);
        return "Company Account created successfully";
    }

    @Override
    public TokenResponse loginWithCompanyAccount(LoginRequest loginRequest) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        CompanyAccount companyAccount = companyAccountRepository
                .findByUserName(loginRequest.getUserName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!passwordEncoder.matches(loginRequest.getPassword(), companyAccount.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String key = "REFRESH_TOKEN:" + companyAccount.getUsername();
        redisTemplate.delete(key);

        var tokenPair = jwt.generateTokensOfCompanyAccount(companyAccount);

        long expirationDays = 7;
        redisTemplate.opsForValue().set(key, tokenPair.refreshToken().token(), expirationDays, TimeUnit.DAYS);

        return TokenResponse.builder()
                .accessToken(tokenPair.accessToken().token())
                .refreshToken(tokenPair.refreshToken().token())
                .build();
    }
}
