package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    @Query(value = "SELECT SUM(amount) FROM Transaction")
    Double sumTotalTransaction();
}
