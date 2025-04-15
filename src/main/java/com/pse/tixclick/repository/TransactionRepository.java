package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.Transaction;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    @Query(value = "SELECT SUM(amount) FROM transactions\n" +
            "where status = 'SUCCESS'",nativeQuery = true)
    Double sumTotalTransaction();

    @Query(value = "SELECT SUM(amount) FROM transactions\n" +
            "where status = 'SUCCESS' AND contract_payment_id IS NULL",nativeQuery = true)
    Double sumTotalCommsion();

    @Query(value = """
            WITH Months AS (
    SELECT 1 AS month UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL\s
    SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL\s
    SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL\s
    SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
)
SELECT m.month,
       COALESCE(s.total_tickets_sold, 0) AS total_tickets_sold,
       COALESCE(s.total_revenue, 0) AS total_revenue
FROM Months m
LEFT JOIN (
    SELECT MONTH(o.order_date) AS month,
           COUNT(od.ticket_purchase_id) AS total_tickets_sold,
           COALESCE(SUM(o.total_amount), 0) AS total_revenue
    FROM orders o
    JOIN order_detail od ON o.order_id = od.order_id
    JOIN ticket_purchase tp ON od.ticket_purchase_id = tp.ticket_purchase_id
    WHERE YEAR(o.order_date) = YEAR(GETDATE())\s
      AND tp.status = 'PURCHASED'\s
    GROUP BY MONTH(o.order_date)
) s ON m.month = s.month
ORDER BY m.month;
    """, nativeQuery = true)
    List<Object[]> getMonthlySalesReport();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "JOIN t.payment p " +
            "JOIN p.order o " +
            "JOIN o.orderDetails od " +
            "JOIN od.ticketPurchase tp " +
            "WHERE tp.event.eventId = :eventId")
    Double getTotalAmountByEventId(@Param("eventId") int eventId);


}
