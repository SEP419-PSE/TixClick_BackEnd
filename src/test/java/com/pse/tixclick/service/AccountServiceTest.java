package com.pse.tixclick.service;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.AccountDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.payload.entity.entity_enum.ERole;
import com.pse.tixclick.payload.request.create.CreateAccountRequest;
import com.pse.tixclick.payload.request.update.UpdateAccountRequest;
import com.pse.tixclick.payload.response.SearchAccountResponse;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.RoleRepository;
import com.pse.tixclick.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;


    @InjectMocks
    private AccountServiceImpl accountService;

    private CreateAccountRequest createAccountRequest;
    private Account account;
    private Role role;
    private AccountDTO accountDTO;

    @BeforeEach
    void setUp() {
        // Create request
        createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setEmail("test@example.com");
        createAccountRequest.setUsername("testuser");
        createAccountRequest.setRole(ERole.BUYER);

        // Expected Account entity
        account = new Account();
        account.setEmail("test@example.com");
        account.setUserName("testuser");

        // Expected Role
        role = new Role();
        role.setRoleId(1);
        role.setRoleName(ERole.BUYER);



        // Expected DTO result
        accountDTO = new AccountDTO();
        accountDTO.setEmail("test@example.com");
        accountDTO.setUserName("testuser");
    }

    @Test
    void createAccount_Success() {
        // Arrange
        when(accountRepository.existsAccountByEmail(createAccountRequest.getEmail())).thenReturn(false);
        when(accountRepository.existsAccountByUserName(createAccountRequest.getUsername())).thenReturn(false);
        when(roleRepository.findRoleByRoleName(ERole.BUYER)).thenReturn(Optional.of(role));
        when(modelMapper.map(createAccountRequest, Account.class)).thenReturn(account);
        when(modelMapper.map(account, AccountDTO.class)).thenReturn(accountDTO);

        // Act
        AccountDTO result = accountService.createAccount(createAccountRequest);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("testuser", result.getUserName());

        verify(accountRepository).save(account);
        verify(accountRepository).existsAccountByEmail("test@example.com");
        verify(accountRepository).existsAccountByUserName("testuser");
        verify(roleRepository).findRoleByRoleName(ERole.BUYER);
    }

    @Test
    void createAccount_EmailAlreadyExists_ThrowsException() {
        when(accountRepository.existsAccountByEmail(createAccountRequest.getEmail())).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> {
            accountService.createAccount(createAccountRequest);
        });

        assertEquals(ErrorCode.EMAIL_TAKEN, exception.getErrorCode());
        verify(accountRepository).existsAccountByEmail("test@example.com");
        verify(accountRepository, never()).existsAccountByUserName(anyString());
        verify(roleRepository, never()).findRoleByRoleName(any());
    }
    @Test
    void createAccount_UsernameAlreadyExists_ThrowsException() {
        when(accountRepository.existsAccountByEmail(createAccountRequest.getEmail())).thenReturn(false);
        when(accountRepository.existsAccountByUserName(createAccountRequest.getUsername())).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> {
            accountService.createAccount(createAccountRequest);
        });

        assertEquals(ErrorCode.USER_EXISTED, exception.getErrorCode());
        verify(accountRepository).existsAccountByEmail("test@example.com");
        verify(accountRepository).existsAccountByUserName("testuser");
        verify(roleRepository, never()).findRoleByRoleName(any());
    }
    @Test
    void createAccount_RoleNotFound_ThrowsException() {
        when(accountRepository.existsAccountByEmail(createAccountRequest.getEmail())).thenReturn(false);
        when(accountRepository.existsAccountByUserName(createAccountRequest.getUsername())).thenReturn(false);
        when(roleRepository.findRoleByRoleName(ERole.BUYER)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            accountService.createAccount(createAccountRequest);
        });

        assertEquals(ErrorCode.ROLE_NOT_FOUND, exception.getErrorCode());
        verify(roleRepository).findRoleByRoleName(ERole.BUYER);
    }

    @Test
    void updateProfile_Success() {
        // Arrange
        String username = "testuser";
        UpdateAccountRequest updateRequest = new UpdateAccountRequest();
        updateRequest.setFirstName("John");
        updateRequest.setLastName("Doe");
        updateRequest.setEmail("new@example.com");
        updateRequest.setPhone("123456789");
        updateRequest.setDob(LocalDate.of(2000, 1, 1));
        updateRequest.setAvatarURL("http://example.com/avatar.png");

        Account user = new Account();
        user.setUserName(username);

        AccountDTO expectedDTO = new AccountDTO();
        expectedDTO.setUserName(username);
        expectedDTO.setEmail("new@example.com");

        // Mock SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);

        when(accountRepository.findAccountByUserName(username)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, AccountDTO.class)).thenReturn(expectedDTO);

        // Act
        AccountDTO result = accountService.updateProfile(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertFalse(user.isActive()); // Email thay đổi => active = false
        verify(accountRepository).save(user);
    }
    @Test
    void updateProfile_UserNotFound_ThrowsException() {
        String username = "nonexistent";
        UpdateAccountRequest updateRequest = new UpdateAccountRequest();

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);

        when(accountRepository.findAccountByUserName(username)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            accountService.updateProfile(updateRequest);
        });

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }


    @Test
    void updateProfile_AllFieldsNull_NoUpdateExceptSave() {
        String username = "testuser";
        UpdateAccountRequest updateRequest = new UpdateAccountRequest(); // All fields null

        Account user = new Account();
        user.setUserName(username);
        user.setActive(true); // Email không đổi thì vẫn true

        AccountDTO expectedDTO = new AccountDTO();
        expectedDTO.setUserName(username);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);

        when(accountRepository.findAccountByUserName(username)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, AccountDTO.class)).thenReturn(expectedDTO);

        AccountDTO result = accountService.updateProfile(updateRequest);

        assertNotNull(result);
        assertTrue(user.isActive());
        verify(accountRepository).save(user);
    }

    @Test
    void getAllAccount_Success() {
        // Arrange
        Account account1 = new Account();
        account1.setAccountId(1);
        account1.setUserName("user1");
        account1.setEmail("user1@example.com");

        Account account2 = new Account();
        account2.setAccountId(2);
        account2.setUserName("user2");
        account2.setEmail("user2@example.com");

        AccountDTO dto1 = new AccountDTO();
        dto1.setAccountId(1);
        dto1.setUserName("user1");
        dto1.setEmail("user1@example.com");

        AccountDTO dto2 = new AccountDTO();
        dto2.setAccountId(2);
        dto2.setUserName("user2");
        dto2.setEmail("user2@example.com");

        List<Account> accounts = List.of(account1, account2);
        when(accountRepository.findAll()).thenReturn(accounts);
        when(modelMapper.map(account1, AccountDTO.class)).thenReturn(dto1);
        when(modelMapper.map(account2, AccountDTO.class)).thenReturn(dto2);

        // Act
        List<AccountDTO> result = accountService.getAllAccount();

        // Assert
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUserName());
        assertEquals("user2", result.get(1).getUserName());

        verify(accountRepository).findAll();
        verify(modelMapper).map(account1, AccountDTO.class);
        verify(modelMapper).map(account2, AccountDTO.class);
    }

    @Test
    void getAllAccount_EmptyList() {
        when(accountRepository.findAll()).thenReturn(Collections.emptyList());

        List<AccountDTO> result = accountService.getAllAccount();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(accountRepository).findAll();
        // Không cần verify mapper vì không map gì cả
    }

    @Test
    void myProfile_Success() {
        // Mock SecurityContext để giả lập user đang đăng nhập
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);

        // Giả lập dữ liệu từ DB
        Account account = new Account();
        account.setUserName("testuser");
        account.setEmail("test@example.com");

        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setUserName("testuser");
        accountDTO.setEmail("test@example.com");

        when(accountRepository.findAccountByUserName("testuser")).thenReturn(Optional.of(account));
        when(modelMapper.map(account, AccountDTO.class)).thenReturn(accountDTO);

        // Gọi hàm cần test
        AccountDTO result = accountService.myProfile();

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUserName());
        assertEquals("test@example.com", result.getEmail());

        verify(accountRepository).findAccountByUserName("testuser");
        verify(modelMapper).map(account, AccountDTO.class);
    }

    @Test
    void myProfile_ThrowsException_WhenUserNotFound() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("unknownUser");
        SecurityContextHolder.setContext(securityContext);

        when(accountRepository.findAccountByUserName("unknownUser")).thenReturn(Optional.empty());

        // Assert
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.myProfile();
        });

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
        verify(accountRepository).findAccountByUserName("unknownUser");
    }


    @Test
    void changePasswordWithOtp_Success() {
        String email = "test@example.com";
        String oldPassword = "oldPass";
        String newPassword = "newPass";

        Account account = new Account();
        account.setEmail(email);
        account.setPassword("hashedOldPassword");

        when(accountRepository.findAccountByEmail(email)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(oldPassword, "hashedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("hashedNewPassword");

        boolean result = accountService.changePasswordWithOtp(email, newPassword, oldPassword);

        assertTrue(result);
        assertEquals("hashedNewPassword", account.getPassword());
        verify(accountRepository).save(account);
    }

    @Test
    void changePasswordWithOtp_WrongOldPassword_ThrowsException() {
        String email = "test@example.com";
        String oldPassword = "wrongOldPassword";
        String newPassword = "newPass";

        Account account = new Account();
        account.setEmail(email);
        account.setPassword("hashedOldPassword");

        when(accountRepository.findAccountByEmail(email)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(oldPassword, "hashedOldPassword")).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> {
            accountService.changePasswordWithOtp(email, newPassword, oldPassword);
        });

        assertEquals(ErrorCode.PASSWORD_NOT_CORRECT, exception.getErrorCode());
        verify(accountRepository, never()).save(any());
    }


    @Test
    void countTotalBuyers_ShouldReturnValue_WhenRepositoryReturnsValue() {
        // Arrange
        when(accountRepository.countTotalBuyers()).thenReturn(10);

        // Act
        int result = accountService.countTotalBuyers();

        // Assert
        assertEquals(10, result);
        verify(accountRepository).countTotalBuyers();
    }


    @Test
    void countTotalBuyers_ShouldReturnZero_WhenRepositoryReturnsNull() {
        // Arrange
        when(accountRepository.countTotalBuyers()).thenReturn(null);

        // Act
        int result = accountService.countTotalBuyers();

        // Assert
        assertEquals(0, result);
        verify(accountRepository).countTotalBuyers();
    }

    @Test
    void getAccountsByRoleManagerAndAdmin_ShouldReturnMappedAccountDTOList() {
        // Arrange
        Account account1 = new Account();
        account1.setUserName("manager1");
        account1.setEmail("manager1@example.com");

        Account account2 = new Account();
        account2.setUserName("admin1");
        account2.setEmail("admin1@example.com");

        List<Account> mockAccounts = List.of(account1, account2);

        AccountDTO dto1 = new AccountDTO();
        dto1.setUserName("manager1");
        dto1.setEmail("manager1@example.com");

        AccountDTO dto2 = new AccountDTO();
        dto2.setUserName("admin1");
        dto2.setEmail("admin1@example.com");

        when(accountRepository.findAccountsByRoleManagerAndAdmin()).thenReturn(mockAccounts);
        when(modelMapper.map(account1, AccountDTO.class)).thenReturn(dto1);
        when(modelMapper.map(account2, AccountDTO.class)).thenReturn(dto2);

        // Act
        List<AccountDTO> result = accountService.getAccountsByRoleManagerAndAdmin();

        // Assert
        assertEquals(2, result.size());
        assertEquals("manager1@example.com", result.get(0).getEmail());
        assertEquals("admin1@example.com", result.get(1).getEmail());

        verify(accountRepository).findAccountsByRoleManagerAndAdmin();
        verify(modelMapper).map(account1, AccountDTO.class);
        verify(modelMapper).map(account2, AccountDTO.class);
    }

    @Test
    void registerPinCode_SuccessfulRegistration() {
        // Arrange
        String pinCode = "123456";
        String username = "testuser";

        // Mock SecurityContext + Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Account account = new Account();
        account.setUserName(username);
        account.setPinCode(null); // No existing PIN

        when(accountRepository.findAccountByUserName(username)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            account.setPinCode(saved.getPinCode()); // Capture the encoded pin
            return saved;
        });

        // Act
        String result = accountService.registerPinCode(pinCode);

        // Assert
        assertEquals("PIN code registered successfully.", result);
        assertNotNull(account.getPinCode());

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        assertTrue(passwordEncoder.matches(pinCode, account.getPinCode())); // Check encoded pin matches

        verify(accountRepository, times(1)).save(account);
    }


    @Test
    void registerPinCode_ShouldFail_WhenPinIsInvalidFormat() {
        // Arrange
        String invalidPin = "abc123"; // hoặc "12345", "1234567", ...

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.registerPinCode(invalidPin);
        });

        assertEquals(ErrorCode.INVALID_PIN_CODE, exception.getErrorCode());
        verify(accountRepository, never()).save(any());
    }


    @Test
    void registerPinCode_ShouldFail_WhenUserNotFound() {
        // Arrange
        String pin = "123456";
        String username = "testuser";

        // Mock SecurityContext và Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Giả lập user không tồn tại
        when(accountRepository.findAccountByUserName(username)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.registerPinCode(pin);
        });

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
        verify(accountRepository, never()).save(any());
    }


    @Test
    void registerPinCode_ShouldFail_WhenUserAlreadyHasPin() {
        // Arrange
        String pin = "123456";
        String username = "testuser";
        Account account = new Account();
        account.setUserName(username);
        account.setPinCode("$2a$10$HashedPin..."); // Giả sử đã có PIN rồi

        // Mock SecurityContext và Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(accountRepository.findAccountByUserName(username)).thenReturn(Optional.of(account));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.registerPinCode(pin);
        });

        assertEquals(ErrorCode.INVALID_PIN_CODE, exception.getErrorCode());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void loginWithPinCode_ShouldSucceed_WhenPinIsCorrect() {
        // Arrange
        String rawPin = "123456";
        String username = "testuser";
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPin = encoder.encode(rawPin);

        Account account = new Account();
        account.setUserName(username);
        account.setPinCode(hashedPin);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(accountRepository.findAccountByUserName(username)).thenReturn(Optional.of(account));

        // Act
        String result = accountService.loginWithPinCode(rawPin);

        // Assert
        assertEquals("Login successful.", result);
    }

    @Test
    void loginWithPinCode_ShouldFail_WhenUserNotFound() {
        // Arrange
        String pin = "123456";
        String username = "ghost";

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(accountRepository.findAccountByUserName(username)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.loginWithPinCode(pin);
        });

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void loginWithPinCode_ShouldFail_WhenUserHasNoPinCode() {
        // Arrange
        String pin = "123456";
        String username = "testuser";

        Account account = new Account();
        account.setUserName(username);
        account.setPinCode(null); // No PIN

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(accountRepository.findAccountByUserName(username)).thenReturn(Optional.of(account));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.loginWithPinCode(pin);
        });

        assertEquals(ErrorCode.INVALID_PIN_CODE, exception.getErrorCode());
    }


    @Test
    void loginWithPinCode_ShouldFail_WhenPinIsIncorrect() {
        // Arrange
        String correctPin = "123456";
        String wrongPin = "000000";
        String username = "testuser";

        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPin = encoder.encode(correctPin); // đúng mã hóa

        Account account = new Account();
        account.setUserName(username);
        account.setPinCode(hashedPin); // gán pin đúng, sẽ so sánh sai bên dưới

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(accountRepository.findAccountByUserName(username)).thenReturn(Optional.of(account));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.loginWithPinCode(wrongPin);
        });

        assertEquals(ErrorCode.INVALID_PIN_CODE, exception.getErrorCode());
    }


    @Test
    void searchAccount_ShouldReturnMatchingAccounts_WhenEmailExists() {
        // Arrange
        String email = "test@example.com";
        Account account = new Account();
        account.setUserName("testuser");
        account.setEmail(email);
        account.setFirstName("Test");
        account.setLastName("User");
        account.setAvatarURL("avatar.jpg");

        when(accountRepository.searchAccountByEmail(email)).thenReturn(List.of(account));

        // Act
        List<SearchAccountResponse> result = accountService.searchAccount(email);

        // Assert
        assertEquals(1, result.size());

        SearchAccountResponse response = result.get(0);
        assertEquals("testuser", response.getUserName());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test", response.getFirstName());
        assertEquals("User", response.getLastName());
        assertEquals("avatar.jpg", response.getAvatar());
    }

    @Test
    void searchAccount_ShouldReturnEmptyList_WhenEmailIsNull() {
        // Act
        List<SearchAccountResponse> result = accountService.searchAccount(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(accountRepository, never()).searchAccountByEmail(any());
    }


    @Test
    void searchAccount_ShouldReturnEmptyList_WhenEmailIsEmpty() {
        // Act
        List<SearchAccountResponse> result = accountService.searchAccount("");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(accountRepository, never()).searchAccountByEmail(any());
    }


    @Test
    void searchAccount_ShouldReturnEmptyList_WhenNoAccountsFound() {
        // Arrange
        String email = "notfound@example.com";
        when(accountRepository.searchAccountByEmail(email)).thenReturn(List.of());

        // Act
        List<SearchAccountResponse> result = accountService.searchAccount(email);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void countTotalAdmins_ShouldReturnCorrectCount() {
        // Arrange
        when(accountRepository.countTotalAdmins()).thenReturn(5);

        // Act
        int result = accountService.countTotalAdmins();

        // Assert
        assertEquals(5, result);
        verify(accountRepository, times(1)).countTotalAdmins();
    }

    @Test
    void countTotalOrganizers_ShouldReturnCorrectCount() {
        // Arrange
        when(accountRepository.countTotalOrganizers()).thenReturn(7);

        // Act
        int result = accountService.countTotalOrganizers();

        // Assert
        assertEquals(7, result);
        verify(accountRepository, times(1)).countTotalOrganizers();
    }
    @Test
    void countTotalManagers_ShouldReturnCorrectCount() {
        // Arrange
        when(accountRepository.countTotalManagers()).thenReturn(5);

        // Act
        int result = accountService.countTotalManagers();

        // Assert
        assertEquals(5, result);
        verify(accountRepository, times(1)).countTotalManagers();
    }
    @Test
    void countTotalAccounts_ShouldReturnCorrectCount() {
        // Arrange
        when(accountRepository.countTotalAccounts()).thenReturn(10);

        // Act
        int result = accountService.countTotalAccounts();

        // Assert
        assertEquals(10, result);
        verify(accountRepository, times(1)).countTotalAccounts();
    }

    @Test
    void countTotalAccounts_ShouldReturnZero_WhenRepositoryReturnsNull() {
        // Arrange
        when(accountRepository.countTotalAccounts()).thenReturn(null);

        // Act
        int result = accountService.countTotalAccounts();

        // Assert
        assertEquals(0, result);
        verify(accountRepository, times(1)).countTotalAccounts();
    }
    @Test
    void countTotalManagers_ShouldReturnZero_WhenRepositoryReturnsNull() {
        // Arrange
        when(accountRepository.countTotalManagers()).thenReturn(null);

        // Act
        int result = accountService.countTotalManagers();

        // Assert
        assertEquals(0, result);
        verify(accountRepository, times(1)).countTotalManagers();
    }
    @Test
    void countTotalOrganizers_ShouldReturnZero_WhenRepositoryReturnsNull() {
        // Arrange
        when(accountRepository.countTotalOrganizers()).thenReturn(null);

        // Act
        int result = accountService.countTotalOrganizers();

        // Assert
        assertEquals(0, result);
        verify(accountRepository, times(1)).countTotalOrganizers();
    }
    @Test
    void countTotalAdmins_ShouldReturnZero_WhenRepositoryReturnsNull() {
        // Arrange
        when(accountRepository.countTotalAdmins()).thenReturn(null);

        // Act
        int result = accountService.countTotalAdmins();

        // Assert
        assertEquals(0, result);
        verify(accountRepository, times(1)).countTotalAdmins();
    }


}
