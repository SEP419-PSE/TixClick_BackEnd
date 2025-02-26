package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}
