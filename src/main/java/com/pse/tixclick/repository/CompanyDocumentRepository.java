package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.company.CompanyDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyDocumentRepository extends JpaRepository<CompanyDocuments,Integer> {
}
