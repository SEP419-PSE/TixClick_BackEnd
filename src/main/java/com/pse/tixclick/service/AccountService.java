package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.AccountDTO;
import com.pse.tixclick.payload.request.create.CreateAccountRequest;
import com.pse.tixclick.payload.request.update.UpdateAccountRequest;

public interface AccountService {
    boolean changePasswordWithOtp(String email, String newPassword, String oldPassword);
    AccountDTO myProfile();

    AccountDTO createAccount(CreateAccountRequest accountDTO);

    AccountDTO updateProfile(UpdateAccountRequest accountDTO);
}
