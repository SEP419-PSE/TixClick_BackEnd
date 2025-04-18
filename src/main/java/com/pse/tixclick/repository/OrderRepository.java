package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.Order;
import com.pse.tixclick.payload.response.RevenueByDateProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("select o from Order o where o.account.accountId = :id")
    List<Order> findByAccountId(@Param("id") int id);

    @Query("select o from Order o where o.account.accountId = :id and o.status = :status")
    List<Order> findByStatusOfAccount(@Param("id") int id, @Param("status") String status);

    @Query(value = "SELECT COALESCE(SUM(o.total_amount), 0) AS total_revenue\n" +
            "FROM orders o\n" +
            "JOIN order_detail od ON o.order_id = od.order_id\n" +
            "JOIN ticket_purchase tp ON od.ticket_purchase_id = tp.ticket_purchase_id\n" +
            "WHERE o.status = 'SUCCESSFUL'" +
            "AND tp.event_id = :eventId", nativeQuery = true)
    Double sumTotalTransaction(@Param("eventId") int eventId);

    @Query(value = """
    WITH DateRange AS (
        SELECT CAST(:startDate AS DATE) AS SaleDate
        UNION ALL
        SELECT DATEADD(DAY, 1, SaleDate)
        FROM DateRange
        WHERE SaleDate < :endDate
    ),
    RevenueByDate AS (
        SELECT
            CAST(o.order_date AS DATE) AS SaleDate,
            SUM(od.amount) AS TotalRevenue
        FROM orders o
        JOIN order_detail od ON o.order_id = od.order_id
        JOIN ticket_purchase tp ON od.ticket_purchase_id = tp.ticket_purchase_id
        WHERE
            tp.event_id = :eventId
            AND tp.event_activity_id = :eventActivityId
            AND CAST(o.order_date AS DATE) BETWEEN :startDate AND :endDate
            AND o.status = 'SUCCESSFUL'
        GROUP BY CAST(o.order_date AS DATE)
    )
    SELECT
        d.SaleDate AS orderDay,
        ISNULL(r.TotalRevenue, 0) AS totalRevenue
    FROM DateRange d
    LEFT JOIN RevenueByDate r ON d.SaleDate = r.SaleDate
    ORDER BY d.SaleDate
    OPTION (MAXRECURSION 1000)
""", nativeQuery = true)
    List<RevenueByDateProjection> getRevenueByEventIdAndEventActivityIdAndDateRange(
            @Param("eventId") int eventId,
            @Param("eventActivityId") int eventActivityId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
