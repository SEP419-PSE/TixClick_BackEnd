package com.pse.tixclick.repository;

import com.pse.tixclick.payload.dto.MyTicketFlatDTO;
import com.pse.tixclick.payload.entity.entity_enum.EOrderStatus;
import com.pse.tixclick.payload.entity.entity_enum.ETicketPurchaseStatus;
import com.pse.tixclick.payload.entity.payment.OrderDetail;
import com.pse.tixclick.payload.response.RevenueByDateProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer>{
    @Query("SELECT od FROM OrderDetail od WHERE od.order.orderId = :id")
    List<OrderDetail> findByOrderId(@Param("id") int orderId);

    Optional<OrderDetail> findOrderDetailByTicketPurchase_TicketPurchaseId(int ticketPurchaseId);

    List<OrderDetail> findOrderDetailsByOrder_OrderId(int orderId);



}
