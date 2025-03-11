package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketPurchaseRepository extends JpaRepository<TicketPurchase, Integer> {
    @Query(value = "SELECT COUNT(*) FROM TicketPurchase WHERE status = 'PURCHASED'")
    int countTotalTicketSold();

    @Query(value = "SELECT COUNT(*) FROM TicketPurchase WHERE status = 'PURCHASED' AND event.eventId = :eventId")
    int countTotalTicketSold(@Param("eventId") int eventId);
    @Query(value = """
            WITH Months AS (
    SELECT 1 AS month UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL\s
    SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL\s
    SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL\s
    SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
)
SELECT m.month,
       COALESCE(s.total_tickets_sold, 0) AS total_tickets_sold
FROM Months m
LEFT JOIN (
    SELECT MONTH(o.order_date) AS month,
           COUNT(od.ticket_purchase_id) AS total_tickets_sold
    FROM orders o
    JOIN order_detail od ON o.order_id = od.order_id
    JOIN ticket_purchase tp ON od.ticket_purchase_id = tp.ticket_purchase_id
    WHERE YEAR(o.order_date) = YEAR(GETDATE())\s
      AND tp.status = 'PURCHASED'\s
    GROUP BY MONTH(o.order_date)
) s ON m.month = s.month
ORDER BY m.month;
        """, nativeQuery = true)
    List<Object[]> countTicketsSoldPerMonth();
}
