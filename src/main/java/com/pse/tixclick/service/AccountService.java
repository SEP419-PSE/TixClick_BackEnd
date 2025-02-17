package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.AccountDTO;
import com.pse.tixclick.payload.request.IntrospectRequest;
import com.pse.tixclick.payload.request.LoginRequest;
import com.pse.tixclick.payload.request.UpdateAccountRequest;
import com.pse.tixclick.payload.response.IntrospectResponse;
import com.pse.tixclick.payload.response.RefreshTokenResponse;
import com.pse.tixclick.payload.response.TokenResponse;

public interface AccountService {
    boolean changePasswordWithOtp(String email, String newPassword, String oldPassword);
    AccountDTO myProfile();



    AccountDTO updateProfile(UpdateAccountRequest accountDTO);
}
