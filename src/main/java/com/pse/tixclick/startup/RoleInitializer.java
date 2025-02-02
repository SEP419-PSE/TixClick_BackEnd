package com.pse.tixclick.startup;

import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleInitializer {
    RoleRepository roleRepository;

    @Bean
    public CommandLineRunner initRoles() {
        return args -> {
            // Danh sách các role cần tạo
            String[] roleNames = {"ADMIN", "BUYER", "ORGANIZER", "MANAGER"};

            Arrays.stream(roleNames).forEach(roleName -> {
                // Sử dụng phương thức findByRoleName
                if (roleRepository.findRoleByRoleName(roleName).isEmpty()) {
                    Role role = new Role();
                    role.setRoleName(roleName);
                    roleRepository.save(role);
                    System.out.println("Role created: " + roleName);
                }
            });
        };
    }
}
