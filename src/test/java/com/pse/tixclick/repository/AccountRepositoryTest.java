package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.payload.entity.entity_enum.ERole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountRepositoryTest {
    @Mock
    private AccountRepository accountRepository;

    private Account account1;
    private Account account2;
    private Role adminRole;
    private Role managerRole;

    @BeforeEach
    void setUp() {
        // Tạo dữ liệu giả lập cho Role
        adminRole = new Role();
        adminRole.setRoleId(1);
        adminRole.setRoleName(ERole.ADMIN);

        managerRole = new Role();
        managerRole.setRoleId(4);
        managerRole.setRoleName(ERole.MANAGER);

        // Tạo dữ liệu giả lập cho Account
        account1 = new Account();
        account1.setAccountId(1);
        account1.setUserName("user1");
        account1.setEmail("user1@gmail.com");
        account1.setPassword("password123");
        account1.setFirstName("John");
        account1.setLastName("Doe");
        account1.setPhone("1234567890");
        account1.setActive(true);
        account1.setDob(LocalDate.of(1990, 1, 1));
        account1.setAvatarURL("http://example.com/avatar1.jpg");
        account1.setRole(adminRole);

        account2 = new Account();
        account2.setAccountId(2);
        account2.setUserName("manager1");
        account2.setEmail("manager1@gmail.com");
        account2.setPassword("password456");
        account2.setFirstName("Jane");
        account2.setLastName("Smith");
        account2.setPhone("0987654321");
        account2.setActive(true);
        account2.setDob(LocalDate.of(1985, 5, 15));
        account2.setAvatarURL("http://example.com/avatar2.jpg");
        account2.setRole(managerRole);
    }

    @Test
    void testFindAccountByUserName_Success() {
        // Mock hành vi của phương thức findAccountByUserName
        when(accountRepository.findAccountByUserName("user1")).thenReturn(Optional.of(account1));

        // Gọi phương thức và kiểm tra
        Optional<Account> result = accountRepository.findAccountByUserName("user1");

        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getUserName());
        assertEquals("user1@gmail.com", result.get().getEmail());
        assertEquals(ERole.ADMIN, result.get().getRole().getRoleName());
    }

    @Test
    void testFindAccountByUserName_NotFound() {
        // Mock hành vi khi không tìm thấy tài khoản
        when(accountRepository.findAccountByUserName("unknown")).thenReturn(Optional.empty());

        // Gọi phương thức và kiểm tra
        Optional<Account> result = accountRepository.findAccountByUserName("unknown");

        assertFalse(result.isPresent());
    }

    @Test
    void testExistsAccountByEmail_Exists() {
        // Mock hành vi của phương thức existsAccountByEmail
        when(accountRepository.existsAccountByEmail("user1@gmail.com")).thenReturn(true);

        // Gọi phương thức và kiểm tra
        boolean exists = accountRepository.existsAccountByEmail("user1@gmail.com");

        assertTrue(exists);
    }
    @Test
    void testExistsAccountByEmail_NotExists() {
        // Mock hành vi khi email không tồn tại
        when(accountRepository.existsAccountByEmail("unknown@gmail.com")).thenReturn(false);

        // Gọi phương thức và kiểm tra
        boolean exists = accountRepository.existsAccountByEmail("unknown@gmail.com");

        assertFalse(exists);
    }

    @Test
    void testFindManagerWithLeastVerifications_Success() {
        // Mock hành vi của phương thức findManagerWithLeastVerifications
        when(accountRepository.findManagerWithLeastVerifications()).thenReturn(Optional.of(account2));

        // Gọi phương thức và kiểm tra
        Optional<Account> result = accountRepository.findManagerWithLeastVerifications();

        assertTrue(result.isPresent());
        assertEquals("manager1", result.get().getUserName());
        assertEquals(ERole.MANAGER, result.get().getRole().getRoleName());
        assertEquals(4, result.get().getRole().getRoleId());
    }

    @Test
    void testFindAccountsByRoleManagerAndAdmin() {
        // Mock hành vi của phương thức findAccountsByRoleManagerAndAdmin
        List<Account> accounts = Arrays.asList(account1, account2);
        when(accountRepository.findAccountsByRoleManagerAndAdmin()).thenReturn(accounts);

        // Gọi phương thức và kiểm tra
        List<Account> result = accountRepository.findAccountsByRoleManagerAndAdmin();

        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUserName());
        assertEquals(ERole.ADMIN, result.get(0).getRole().getRoleName());
        assertEquals("manager1", result.get(1).getUserName());
        assertEquals(ERole.MANAGER, result.get(1).getRole().getRoleName());
    }

    @Test
    void testCountTotalAdmins() {
        // Mock hành vi của phương thức countTotalAdmins
        when(accountRepository.countTotalAdmins()).thenReturn(5);

        // Gọi phương thức và kiểm tra
        int count = accountRepository.countTotalAdmins();

        assertEquals(5, count);
    }

    @Test
    void testSearchAccountByEmail() {
        // Mock hành vi của phương thức searchAccountByEmail
        List<Account> accounts = Arrays.asList(account1);
        when(accountRepository.searchAccountByEmail("user1")).thenReturn(accounts);

        // Gọi phương thức và kiểm tra
        List<Account> result = accountRepository.searchAccountByEmail("user1");

        assertEquals(1, result.size());
        assertEquals("user1@gmail.com", result.get(0).getEmail());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Doe", result.get(0).getLastName());
    }

    @Test
    void testFindAccountsByRole_RoleId() {
        // Mock hành vi của phương thức findAccountsByRole_RoleId
        List<Account> accounts = Arrays.asList(account2);
        when(accountRepository.findAccountsByRole_RoleId(4)).thenReturn(accounts);

        // Gọi phương thức và kiểm tra
        List<Account> result = accountRepository.findAccountsByRole_RoleId(4);

        assertEquals(1, result.size());
        assertEquals("manager1", result.get(0).getUserName());
        assertEquals(ERole.MANAGER, result.get(0).getRole().getRoleName());
    }
}
