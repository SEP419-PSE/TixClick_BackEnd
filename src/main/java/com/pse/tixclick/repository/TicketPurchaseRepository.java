package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    @Query(value = """
    WITH DateRange AS (
        SELECT DATEADD(DAY, -:date, CAST(GETDATE() AS DATE)) AS order_date
        UNION ALL
        SELECT DATEADD(DAY, 1, order_date)
        FROM DateRange
        WHERE DATEADD(DAY, 1, order_date) <= CAST(GETDATE() AS DATE)
    )
    SELECT
        dr.order_date,
        COALESCE(SUM(o.total_amount), 0) AS total_revenue,
        COUNT(DISTINCT od.ticket_purchase_id) AS total_tickets_sold,
        COUNT(DISTINCT ea.event_id) AS total_events_created
    FROM DateRange dr
    LEFT JOIN orders o
        ON CONVERT(DATE, o.order_date) = dr.order_date
        AND o.status = 'SUCCESSFUL'
    LEFT JOIN order_detail od
        ON o.order_id = od.order_id
    LEFT JOIN ticket_purchase tp
        ON od.ticket_purchase_id = tp.ticket_purchase_id
        AND tp.status = 'PURCHASED'
    LEFT JOIN event_activity ea
        ON CONVERT(DATE, ea.date_event) = dr.order_date
    GROUP BY dr.order_date
    ORDER BY dr.order_date
    """, nativeQuery = true)
    List<Object[]> countTicketsSoldAndRevenueByDay(@Param("date") int date);


    @Query(value = """
    WITH DateRange AS (
        SELECT DATEADD(DAY, -:date, CAST(GETDATE() AS DATE)) AS order_date
        UNION ALL
        SELECT DATEADD(DAY, 1, order_date)
        FROM DateRange
        WHERE DATEADD(DAY, 1, order_date) <= :endDate
    )
    SELECT
        dr.order_date,
        COALESCE(SUM(o.total_amount), 0) AS total_revenue,
        COUNT(DISTINCT od.ticket_purchase_id) AS total_tickets_sold,
        COUNT(DISTINCT ea.event_id) AS total_events_created
    FROM DateRange dr
    LEFT JOIN orders o
        ON CONVERT(DATE, o.order_date) = dr.order_date
        AND o.status = 'SUCCESSFUL'
    LEFT JOIN order_detail od
        ON o.order_id = od.order_id
    LEFT JOIN ticket_purchase tp
        ON od.ticket_purchase_id = tp.ticket_purchase_id
        AND tp.status = 'PURCHASED'
    LEFT JOIN event_activity ea
        ON CONVERT(DATE, ea.date_event) = dr.order_date
    GROUP BY dr.order_date
    ORDER BY dr.order_date
    """, nativeQuery = true)
    List<Object[]> countTicketsSoldAndRevenueByDayAfter(@Param("date") int date, @Param("endDate") LocalDate endDate);
}
