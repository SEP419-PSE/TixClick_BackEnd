package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.company.ContractDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractDocumentRepository extends JpaRepository<ContractDocument,Integer> {
    @Query("SELECT c FROM ContractDocument c WHERE c.contract.contractId = :id")
    List<ContractDocument> findByAllByContractId(@Param("id") int contractId);

    @Query("SELECT c FROM ContractDocument c WHERE c.contract.company.companyId = :id")
    List<ContractDocument> findByAllByCompanyId(@Param("id") int companyId);

    @Query("SELECT c FROM ContractDocument c WHERE c.contract.event.eventId = :id")
    List<ContractDocument> findByAllByEventId(@Param("id") int eventId);

    @Query("SELECT c FROM ContractDocument c WHERE c.fileName = :name")
    Optional<ContractDocument> findByFileName(@Param("name") String name);
}
