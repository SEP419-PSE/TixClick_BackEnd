package com.pse.tixclick.service;

import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SimpleAccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    public void testChangePasswordWithOtp() {
        // Tạo dữ liệu test
        String email = "test@example.com";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        // Tạo account test
        Account testAccount = new Account();
        testAccount.setEmail(email);
        testAccount.setPassword("encodedOldPassword");

        // Setup mock
        when(accountRepository.findAccountByEmail(email))
                .thenReturn(Optional.of(testAccount));

        when(passwordEncoder.matches(oldPassword, testAccount.getPassword()))
                .thenReturn(true);

        // Execute
        boolean result = accountService.changePasswordWithOtp(email, newPassword, oldPassword);

        // Verify
        assertTrue(result);
        verify(accountRepository).save(any(Account.class));
    }
}