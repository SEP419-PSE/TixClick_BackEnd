package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.company.ContractVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractVerificationRepository extends JpaRepository<ContractVerification,Integer> {
    ContractVerification findByContract_ContractId(int contractId);
}
