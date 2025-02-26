package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer>{
}
