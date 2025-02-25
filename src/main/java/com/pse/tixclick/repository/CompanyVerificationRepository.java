package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.company.CompanyVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyVerificationRepository extends JpaRepository<CompanyVerification,Integer> {
    Optional<CompanyVerification> findCompanyVerificationByCompanyVerificationIdAndAccount_UserName(int companyVerificationId, String userName);
}
