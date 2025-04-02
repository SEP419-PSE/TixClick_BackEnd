package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.AccountDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.payload.request.create.CreateAccountRequest;
import com.pse.tixclick.payload.request.update.UpdateAccountRequest;
import com.pse.tixclick.payload.response.SearchAccountResponse;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.CompanyRepository;
import com.pse.tixclick.repository.MemberRepository;
import com.pse.tixclick.repository.RoleRepository;
import com.pse.tixclick.service.AccountService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper accountMapper;

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

    @Override
    public List<AccountDTO> getAllAccount() {
        List<Account> accounts = accountRepository.findAll();

        return accounts.stream()
                .map(account -> accountMapper.map(account, AccountDTO.class))
                .toList();
    }

    @Override
    public int countTotalBuyers() {
        return Optional.of(accountRepository.countTotalBuyers()).orElse(0);
    }

    @Override
    public int countTotalAdmins() {
        return Optional.of(accountRepository.countTotalAdmins()).orElse(0);
    }

    @Override
    public int countTotalOrganizers() {
        return Optional.of(accountRepository.countTotalOrganizers()).orElse(0);
    }

    @Override
    public int countTotalManagers() {
        return Optional.of(accountRepository.countTotalManagers()).orElse(0);
    }

    @Override
    public int countTotalAccounts() {
        return Optional.of(accountRepository.countTotalAccounts()).orElse(0);
    }

    @Override
    public List<AccountDTO> getAccountsByRoleManagerAndAdmin() {
        List<Account> accounts = accountRepository.findAccountsByRoleManagerAndAdmin();

        return accounts.stream()
                .map(account -> accountMapper.map(account, AccountDTO.class))
                .toList();
    }

    @Override
    public String registerPinCode(String pinCode) {
        // Kiểm tra tính hợp lệ của mã PIN (phải có đúng 6 chữ số)
        if (pinCode == null || !pinCode.matches("\\d{6}")) {
            throw new AppException(ErrorCode.INVALID_PIN_CODE);
        }

        // Lấy thông tin user từ context
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        Account user = accountRepository.findAccountByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if(user.getPinCode() != null && !user.getPinCode().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_PIN_CODE);
        }
        // Mã hóa PIN bằng BCrypt
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPin = passwordEncoder.encode(pinCode);

        user.setPinCode(hashedPin);
        accountRepository.save(user);

        return "PIN code registered successfully.";
    }

    @Override
    public String loginWithPinCode(String pinCode) {
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        Account user = accountRepository.findAccountByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if(user.getPinCode() == null || user.getPinCode().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_PIN_CODE);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean isPinValid = passwordEncoder.matches(pinCode, user.getPinCode());
        if (!isPinValid) {
            throw new AppException(ErrorCode.INVALID_PIN_CODE);
        }

        return "Login successful.";

    }

    @Override
    public SearchAccountResponse searchAccount(String email) {
        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return SearchAccountResponse.builder()
                .userName(account.getUserName())
                .email(account.getEmail())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .avatar(account.getAvatarURL())
                .build();
    }

    @Override
    public SearchAccountResponse searchAccountWithCompany(String email) {
        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));


        return SearchAccountResponse.builder()
                .userName(account.getUserName())
                .email(account.getEmail())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .avatar(account.getAvatarURL())
                .build();
    }


}
