package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.payload.entity.entity_enum.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findRoleByRoleName(ERole roleName);
}
