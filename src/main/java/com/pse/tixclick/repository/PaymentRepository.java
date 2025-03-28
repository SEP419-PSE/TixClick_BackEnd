package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    @Query("select p from Payment p where p.order.orderId = :id")
    List<Payment> findByOrderId(@Param("id") int id);

    @Query("SELECT p from Payment p where p.orderCode = :code")
    Payment findPaymentByOrderCode(@Param("code") String code);
}
