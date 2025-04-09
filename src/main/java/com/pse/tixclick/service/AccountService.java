package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.AccountDTO;
import com.pse.tixclick.payload.request.create.CreateAccountRequest;
import com.pse.tixclick.payload.request.update.UpdateAccountRequest;
import com.pse.tixclick.payload.response.SearchAccountResponse;

import java.util.List;

public interface
AccountService {
    boolean changePasswordWithOtp(String email, String newPassword, String oldPassword);
    AccountDTO myProfile();

    AccountDTO createAccount(CreateAccountRequest accountDTO);

    AccountDTO updateProfile(UpdateAccountRequest accountDTO);

    List<AccountDTO> getAllAccount();

    int countTotalBuyers();

    int countTotalAdmins();

    int countTotalOrganizers();

    int countTotalManagers();

    int countTotalAccounts();

    List<AccountDTO> getAccountsByRoleManagerAndAdmin();

    String registerPinCode(String pinCode);
    String loginWithPinCode(String pinCode);

    List<SearchAccountResponse> searchAccount(String email);



}
