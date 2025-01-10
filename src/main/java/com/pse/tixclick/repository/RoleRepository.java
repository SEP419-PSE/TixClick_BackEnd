package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findRoleByRoleName(String roleName);
}
