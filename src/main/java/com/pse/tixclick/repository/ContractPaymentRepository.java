package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.ContractPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractPaymentRepository extends JpaRepository<ContractPayment, Integer> {
}
