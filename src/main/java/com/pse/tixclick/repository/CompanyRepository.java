package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Integer> {
    Optional<Company> findCompanyByCompanyIdAndRepresentativeId_UserName(int companyId, String userName);

    Optional<Company> findCompanyByCompanyId(int companyId);
}
