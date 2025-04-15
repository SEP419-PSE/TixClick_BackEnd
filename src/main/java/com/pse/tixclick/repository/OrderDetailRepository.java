package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.OrderDetail;
import com.pse.tixclick.payload.response.RevenueByDateProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer>{
    @Query("SELECT od FROM OrderDetail od WHERE od.order.orderId = :id")
    List<OrderDetail> findByOrderId(@Param("id") int orderId);




}
