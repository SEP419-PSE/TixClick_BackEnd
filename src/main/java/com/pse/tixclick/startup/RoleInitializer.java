package com.pse.tixclick.startup;

import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.payload.entity.event.EventCategory;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.EventCategoryRepository;
import com.pse.tixclick.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleInitializer {
    RoleRepository roleRepository;
    AccountRepository accountRepository;
    EventCategoryRepository eventCategoryRepository;
    @Bean
    public CommandLineRunner initRolesAndAdmin() {
        return args -> {
            // Danh sách các role cần tạo
            String[] roleNames = {"ADMIN", "BUYER", "ORGANIZER", "MANAGER"};
            String[] categoryNames = {"Music", "Sport", "Theater",  "Other"};
            Arrays.stream(categoryNames).forEach(categoryName -> {
                if (eventCategoryRepository.findEventCategoriesByCategoryName(categoryName).isEmpty()) {
                    EventCategory category = new EventCategory();
                    category.setCategoryName(categoryName);
                    eventCategoryRepository.save(category);
                    System.out.println("Category Event created: " + categoryName);
                }
            });

            Arrays.stream(roleNames).forEach(roleName -> {
                if (roleRepository.findRoleByRoleName(roleName).isEmpty()) {
                    Role role = new Role();
                    role.setRoleName(roleName);
                    roleRepository.save(role);
                    System.out.println("Role created: " + roleName);
                }
            });

            // Tạo tài khoản admin nếu chưa tồn tại
            if (accountRepository.findAccountByUserName("admin").isEmpty()) {
                Role adminRole = roleRepository.findRoleByRoleName("ADMIN")
                        .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));

                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

                Account adminAccount = new Account();
                adminAccount.setUserName("admin");
                adminAccount.setPassword(passwordEncoder.encode("admin"));
                adminAccount.setEmail("admin@example.com");
                adminAccount.setFirstName("System");
                adminAccount.setLastName("Admin");
                adminAccount.setActive(true);
                adminAccount.setRole(adminRole);

                accountRepository.save(adminAccount);
                System.out.println("Admin account created: admin/admin");
            }
        };
    }
}
