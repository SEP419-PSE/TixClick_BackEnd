package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
}
