package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
}
