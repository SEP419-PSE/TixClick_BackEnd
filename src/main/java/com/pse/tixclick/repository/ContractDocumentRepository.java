package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.company.ContractDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractDocumentRepository extends JpaRepository<ContractDocument,Integer> {
}
