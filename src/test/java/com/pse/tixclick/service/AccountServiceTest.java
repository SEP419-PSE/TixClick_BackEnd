package com.pse.tixclick.service;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.AccountDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.payload.entity.entity_enum.ERole;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.RoleRepository;
import com.pse.tixclick.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder; // ✅ Dùng mock

    @InjectMocks
    private AccountServiceImpl accountService;


    private Account account;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setRoleId(1);
        role.setRoleName(ERole.ADMIN);

        account = new Account();
        account.setAccountId(1);
        account.setUserName("user1");
        account.setEmail("user1@gmail.com");
        account.setPassword("encodedOldPassword"); // giả lập password đã mã hóa
        account.setFirstName("John");
        account.setLastName("Doe");
        account.setPhone("1234567890");
        account.setActive(true);
        account.setDob(LocalDate.of(1990, 1, 1));
        account.setAvatarURL("http://example.com/avatar1.jpg");
        account.setRole(role);
    }

    @Test
    void testChangePasswordWithOtp_Success() {
        String email = "user1@gmail.com";
        String oldPassword = "123456";
        String newPassword = "newPassword123";

        // ✅ Giả sử service gọi email.toLowerCase().trim()
        String matchedEmail = email.trim().toLowerCase();

        when(accountRepository.findAccountByEmail(eq(matchedEmail))).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(eq(oldPassword), eq("encodedOldPassword"))).thenReturn(true);
        when(passwordEncoder.encode(eq(newPassword))).thenReturn("encodedNewPassword");

        boolean result = accountService.changePasswordWithOtp(email, newPassword, oldPassword);

        assertTrue(result);
        assertEquals("encodedNewPassword", account.getPassword());
        verify(accountRepository).save(account);
    }

    @Test
    void testChangePasswordWithOtp_UserNotFound() {
        String email = "user1@gmail.com";
        String oldPassword = "123456";
        String newPassword = "newPassword123";

        when(accountRepository.findAccountByEmail(email)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () ->
                accountService.changePasswordWithOtp(email, newPassword, oldPassword));

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testChangePasswordWithOtp_WrongOldPassword() {
        String email = "user1@gmail.com";
        String oldPassword = "wrongPassword";
        String newPassword = "newPassword123";

        when(accountRepository.findAccountByEmail(email)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(eq(oldPassword), eq("encodedOldPassword"))).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () ->
                accountService.changePasswordWithOtp(email, newPassword, oldPassword));

        assertEquals(ErrorCode.PASSWORD_NOT_CORRECT, exception.getErrorCode());
        verify(accountRepository, never()).save(any(Account.class));
    }
}
