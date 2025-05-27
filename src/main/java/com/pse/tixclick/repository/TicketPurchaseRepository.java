package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.entity_enum.ETicketPurchaseStatus;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import com.pse.tixclick.payload.response.RevenueByDateProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
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

    @Query(value = "SELECT tp FROM TicketPurchase tp WHERE tp.account.accountId = :accountId")
    List<TicketPurchase> getTicketPurchasesByAccount(@Param("accountId") int accountId);

    int countTicketPurchasesByStatus(ETicketPurchaseStatus status);

    @Query(value = "SELECT \n" +
            "    SUM(tp.quantity * t.price) AS totalPrice\n" +
            "FROM \n" +
            "    ticket_purchase tp\n" +
            "JOIN \n" +
            "    ticket t ON tp.ticket_id = t.ticket_id\n" +
            "JOIN \n" +
            "    events e ON tp.event_id = e.event_id\n" +
            "WHERE\n" +
            "    tp.status = 'PURCHASED'  -- Assuming you only want to calculate for successful purchases\n" +
            "    AND e.event_id = :eventId  -- Replace YOUR_EVENT_ID with the specific event id you want to filter by\n" +
            "GROUP BY\n" +
            "    e.event_id, e.event_name\n" +
            "ORDER BY\n" +
            "    totalPrice DESC;", nativeQuery = true)
    Double getTotalPriceByEventId(@Param("eventId") int eventId);
    @Query(value = "SELECT \n" +
            "    SUM(tp.quantity) AS totalTicketsSold\n" +
            "FROM \n" +
            "    ticket_purchase tp\n" +
            "JOIN \n" +
            "    events e ON tp.event_id = e.event_id\n" +
            "WHERE\n" +
            "    tp.status = 'PURCHASED'  -- Chỉ đếm vé đã mua thành công\n" +
            "    AND e.event_id = :eventId  -- Chỉ lấy sự kiện với event_id = 6\n" +
            "GROUP BY\n" +
            "    e.event_id, e.event_name;", nativeQuery = true)
    Integer getTotalTicketsSoldByEventId(@Param("eventId") int eventId);

    @Query(value = "select COUNT(ticket_purchase_id) from ticket_purchase\n" +
            "  where event_activity_id = :eventActivityId and ticket_id = :ticketId and status = 'PURCHASED'",nativeQuery = true)
    int countTicketPurchasedByEventActivityIdAndTicketId(int eventActivityId, int ticketId);

    int countByStatusAndEvent_EventId(ETicketPurchaseStatus status, int eventId);

    List<TicketPurchase> findByEventActivity_EventActivityIdAndStatus(int eventActivityId, ETicketPurchaseStatus status);

    List<TicketPurchase> findTicketPurchasesByEvent_EventIdAndStatus(int eventId, ETicketPurchaseStatus status);

    Optional<TicketPurchase> findByTicketPurchaseIdAndStatusAndAccount_UserName(int ticketPurchaseId, ETicketPurchaseStatus status, String userName);

    @Query(value = """
            WITH Calendar AS (
                SELECT start_ticket_sale AS [date]
                FROM event_activity
                WHERE event_activity_id = :eventActivityId
                UNION ALL
                SELECT DATEADD(DAY, 1, [date])
                FROM Calendar
                WHERE [date] < (
                    SELECT end_ticket_sale 
                    FROM event_activity 
                    WHERE event_activity_id = :eventActivityId
                )
            ),
            RevenuePerDay AS (
                SELECT 
                    CAST(o.order_date AS DATE) AS [date],
                    SUM(tp.quantity * t.price) AS revenue
                FROM 
                    ticket_purchase tp
                    JOIN ticket t ON tp.ticket_id = t.ticket_id
                    JOIN order_detail od ON od.ticket_purchase_id = tp.ticket_purchase_id
                    JOIN orders o ON o.order_id = od.order_id
                WHERE 
                    tp.status = 'PURCHASED'
                    AND o.status = 'SUCCESSFUL'
                    AND tp.event_id = :eventId
                    AND tp.event_activity_id = :eventActivityId
                    AND CAST(o.order_date AS DATE) BETWEEN (
                        SELECT start_ticket_sale 
                        FROM event_activity 
                        WHERE event_activity_id = :eventActivityId
                    ) AND (
                        SELECT end_ticket_sale 
                        FROM event_activity 
                        WHERE event_activity_id = :eventActivityId
                    )
                GROUP BY 
                    CAST(o.order_date AS DATE)
            )
            SELECT 
                c.[date],
                ISNULL(r.revenue, 0) AS revenue
            FROM 
                Calendar c
                LEFT JOIN RevenuePerDay r ON r.[date] = c.[date]
            ORDER BY 
                c.[date]
        """, nativeQuery = true)
    List<RevenueByDateProjection>  getDailyRevenueByEventAndActivity(@Param("eventId") int eventId, @Param("eventActivityId") int eventActivityId);

    Optional<TicketPurchase> findTicketPurchaseByTicketPurchaseId(int ticketPurchaseId);


    Integer countTicketPurchaseByEventActivity_EventActivityIdAndStatus(int eventActivityId, ETicketPurchaseStatus status);

    @Query("SELECT DISTINCT tp.account.accountId FROM TicketPurchase tp " +
            "WHERE tp.status = :status AND tp.eventActivity.eventActivityId = :eventActivityId")
    List<Integer> findDistinctAccountIdsByEventActivityIdAndStatus(
            @Param("eventActivityId") int eventActivityId,
            @Param("status") ETicketPurchaseStatus status);

    List<TicketPurchase> findTicketPurchasesByAccount_AccountIdAndEventActivity_EventActivityIdAndStatus(
            int accountId, int eventActivityId, ETicketPurchaseStatus status);



    @Query(value = """

            SELECT tp.*
    FROM [tixclick].[dbo].[ticket_purchase] tp
    INNER JOIN [tixclick].[dbo].[order_detail] od ON tp.ticket_purchase_id = od.ticket_purchase_id
    INNER JOIN [tixclick].[dbo].[orders] o ON od.order_id = o.order_id
    WHERE o.order_code = :orderCode; 
    """, nativeQuery = true)
    List<TicketPurchase> findTicketPurchaseByOrderCode(
            @Param("orderCode") String orderCode
    );

    @Query("SELECT tp FROM TicketPurchase tp WHERE (tp.status = 'REFUNDING' OR tp.status = 'REFUNDED') AND tp.event.eventId = :eventId")
    List<TicketPurchase> listTicketPurchaseRefunding(@Param("eventId") int eventId);


}
