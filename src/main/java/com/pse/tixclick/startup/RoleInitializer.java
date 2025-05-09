package com.pse.tixclick.startup;

import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.payload.entity.entity_enum.EEventStatus;
import com.pse.tixclick.payload.entity.entity_enum.ERole;
import com.pse.tixclick.payload.entity.entity_enum.ZoneTypeEnum;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventCategory;
import com.pse.tixclick.payload.entity.seatmap.ZoneType;
import com.pse.tixclick.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleInitializer {
    RoleRepository roleRepository;
    AccountRepository accountRepository;
    EventCategoryRepository eventCategoryRepository;
    ZoneTypeRepository zoneTypeRepository;
    EventRepository eventRepository;
    @Bean
    public CommandLineRunner initRolesAndAdmin() {
        return args -> {
            List<ERole> roles = List.of(ERole.ADMIN, ERole.BUYER, ERole.ORGANIZER, ERole.MANAGER);
            String[] categoryNames = {"Music", "Sport", "Theater", "Other"};

            // Kiểm tra danh mục sự kiện
            Arrays.stream(categoryNames).forEach(categoryName -> {
                if (eventCategoryRepository.findEventCategoriesByCategoryName(categoryName).stream().findFirst().isEmpty()) {
                    EventCategory category = new EventCategory();
                    category.setCategoryName(categoryName);
                    eventCategoryRepository.save(category);
                    System.out.println("✅ Category Event created: " + categoryName);
                }
            });

            // Kiểm tra Role
            roles.forEach(roleName -> {
                if (roleRepository.findRoleByRoleName(roleName).isEmpty()) {
                    Role role = new Role();
                    role.setRoleName(roleName);
                    roleRepository.save(role);
                    System.out.println("✅ Role created: " + roleName);
                }
            });

            // Kiểm tra ZoneType
            Arrays.stream(ZoneTypeEnum.values()).forEach(type -> {
                if (zoneTypeRepository.findZoneTypeByTypeName(type).isEmpty()) {
                    ZoneType zoneType = new ZoneType();
                    zoneType.setTypeName(type);
                    zoneTypeRepository.save(zoneType);
                    System.out.println("✅ ZoneType created: " + type);
                }
            });

            // Kiểm tra tài khoản admin
            if (accountRepository.findAccountByUserName("admin").isEmpty()) {
                Role adminRole = roleRepository.findRoleByRoleName(ERole.ADMIN)
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
                System.out.println("✅ Admin account created: admin/admin");
            }
        };


    }
}
