package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.company.CompanyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyAccountRepository extends JpaRepository<CompanyAccount,Integer> {
    @Query("SELECT ca FROM CompanyAccount ca WHERE ca.username = :userName")
    Optional<CompanyAccount> findByUserName(@Param("userName") String userName);
}
