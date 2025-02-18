package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.AccountDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.payload.request.CreateAccountRequest;
import com.pse.tixclick.payload.request.UpdateAccountRequest;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.RoleRepository;
import com.pse.tixclick.service.AccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ModelMapper accountMapper;

    @Override
    public boolean changePasswordWithOtp(String email, String newPassword, String oldPassword) {
        // Retrieve the account based on the email
        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Check if the old password matches the current password
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
            // Handle incorrect old password scenario
            throw new AppException(ErrorCode.PASSWORD_NOT_CORRECT);  // Or another appropriate exception
        }

        // Encode the new password and set it
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
        return true;

    }


    @Override
    public AccountDTO myProfile() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        // Lấy thông tin tài khoản từ database theo username
        var user = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Sử dụng AccountMapper để chuyển đổi đối tượng Account thành AccountDTO
        return accountMapper.map(user,AccountDTO.class);
    }

    @Override
    public AccountDTO createAccount(CreateAccountRequest accountDTO) {
        // Kiểm tra xem email đã tồn tại chưa
        if (accountRepository.existsAccountByEmail(accountDTO.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_TAKEN);
        }

        // Kiểm tra xem username đã tồn tại chưa
        if (accountRepository.existsAccountByUserName(accountDTO.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        Role role = roleRepository.findRoleByRoleName(accountDTO.getRole())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        // Chuyển đổi CreateAccountRequest thành Account
        var account = accountMapper.map(accountDTO, Account.class);
        account.setPassword(new BCryptPasswordEncoder(10).encode("123456"));
        account.setActive(true);
        account.setRole(role);
        // Lưu tài khoản vào database
        accountRepository.save(account);

        // Trả về thông tin tài khoản sau khi tạo
        return accountMapper.map(account, AccountDTO.class);
    }

    @Override
    public AccountDTO updateProfile(UpdateAccountRequest accountDTO) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        // Lấy thông tin tài khoản từ database theo username
        var user = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Cập nhật thông tin tài khoản
        if (accountDTO.getFirstName() != null) {
            user.setFirstName(accountDTO.getFirstName());
        }
        if (accountDTO.getLastName() != null) {
            user.setLastName(accountDTO.getLastName());
        }
        if (accountDTO.getEmail() != null) {
            user.setEmail(accountDTO.getEmail());
            user.setActive(false); // This is set if email is not null
        }
        if (accountDTO.getPhone() != null) {
            user.setPhone(accountDTO.getPhone());
        }
        if (accountDTO.getDob() != null) {
            user.setDob(accountDTO.getDob());
        }
        if (accountDTO.getAvatarURL() != null) {
            user.setAvatarURL(accountDTO.getAvatarURL());
        }


        // Lưu thông tin tài khoản
        accountRepository.save(user);

        // Trả về thông tin tài khoản sau khi cập nhật
        return accountMapper.map(user, AccountDTO.class);
    }

}
