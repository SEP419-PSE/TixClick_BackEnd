import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(value = MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountServiceImpl accountService;

    private final String email = "test@example.com";
    private final String oldPassword = "oldPassword123";
    private final String newPassword = "newPassword456";
    private final String encodedOldPassword = "encodedOldPassword";
    private final String encodedNewPassword = "encodedNewPassword";
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setEmail(email);
        testAccount.setPassword(encodedOldPassword);
    }

    @Test
    void changePasswordWithOtp_Success() {
        // Arrange
        when(accountRepository.findAccountByEmail(email))
                .thenReturn(Optional.of(testAccount));

        when(passwordEncoder.matches(oldPassword, encodedOldPassword))
                .thenReturn(true);

        when(passwordEncoder.encode(newPassword))
                .thenReturn(encodedNewPassword);

        // Act
        boolean result = accountService.changePasswordWithOtp(email, newPassword, oldPassword);

        // Assert
        assertTrue(result);
        assertEquals(encodedNewPassword, testAccount.getPassword());

        // Verify interactions
        verify(accountRepository).findAccountByEmail(email);
        verify(passwordEncoder).matches(oldPassword, encodedOldPassword);
        verify(passwordEncoder).encode(newPassword);
        verify(accountRepository).save(testAccount);
    }
}