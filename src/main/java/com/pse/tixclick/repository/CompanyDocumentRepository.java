package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.company.CompanyDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyDocumentRepository extends JpaRepository<CompanyDocuments,Integer> {
    List<CompanyDocuments> findCompanyDocumentsByCompany_CompanyId(int companyId);
}
