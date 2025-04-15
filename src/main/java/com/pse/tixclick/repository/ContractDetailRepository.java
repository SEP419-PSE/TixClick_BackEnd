package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.company.ContractDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ContractDetailRepository extends JpaRepository<ContractDetail, Integer> {
    @Query(value = "SELECT cd FROM ContractDetail cd WHERE cd.contract.contractId = :contractId")
    List<ContractDetail> findByContractId(@Param("contractId") int contractId);

    @Query(value = "SELECT cd FROM ContractDetail cd WHERE cd.contractPayment.contractPaymentId = :contractPaymentId")
    ContractDetail findByContractPaymentId(@Param("contractPaymentId") int contractPaymentId);

    List<ContractDetail> findContractDetailsByContract_ContractId(int contractId);

}
