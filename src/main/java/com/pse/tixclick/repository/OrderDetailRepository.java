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

    @Query("""
    SELECT CAST(o.orderDate AS date) AS orderDay, SUM(od.amount) AS totalRevenue
    FROM OrderDetail od
    JOIN od.order o
    JOIN od.ticketPurchase tp
    WHERE tp.eventActivity.eventActivityId = :eventActivityId
      AND tp.ticket.ticketId = :ticketId
      AND o.orderDate BETWEEN :startDate AND :endDate
      AND o.status = 'COMPLETED'
    GROUP BY CAST(o.orderDate AS date)
    ORDER BY CAST(o.orderDate AS date)
""")
    List<RevenueByDateProjection> getRevenueByDayForTicket(
            @Param("eventActivityId") int eventActivityId,
            @Param("ticketId") int ticketId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


}
