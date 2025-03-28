package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.ContractPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ContractPaymentRepository extends JpaRepository<ContractPayment, Integer> {
    @Query(value = "SELECT cp FROM ContractPayment cp WHERE cp.contractDetail.contractDetailId = :contractDetailId")
    Optional<ContractPayment> findByContractDetailId(@Param("contractDetailId") int contractDetailId);
}
