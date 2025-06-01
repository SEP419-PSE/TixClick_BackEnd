package com.pse.tixclick.repository;

import com.pse.tixclick.payload.dto.MyTicketFlatDTO;
import com.pse.tixclick.payload.entity.entity_enum.EOrderStatus;
import com.pse.tixclick.payload.entity.entity_enum.ETicketPurchaseStatus;
import com.pse.tixclick.payload.entity.payment.Order;
import com.pse.tixclick.payload.response.RevenueByDateProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    List<Order> findOrderByAccount_AccountIdAndStatus(int accountId, EOrderStatus status);

    @Query("SELECT new com.pse.tixclick.payload.dto.MyTicketFlatDTO(" +
            "o.orderId, o.orderCode, o.orderDate, o.totalAmount, o.totalAmountDiscount, " +
            "tp.ticketPurchaseId, o.qrCode, tp.quantity, t.price, t.ticketName, " +
            "e.eventId, e.eventName, e.logoURL, e.bannerURL, e.locationName, e.category.eventCategoryId, " +
            "e.address, e.ward, e.district, e.city, (e.seatMap IS NOT NULL), " +
            "ea.eventActivityId, ea.dateEvent, ea.startTimeEvent, " +
            "z.zoneName, s.seatName) " +
            "FROM Order o " +
            "JOIN o.orderDetails od " +
            "JOIN od.ticketPurchase tp " +
            "JOIN tp.ticket t " +
            "LEFT JOIN tp.event e " +
            "LEFT JOIN tp.eventActivity ea " +
            "LEFT JOIN tp.zoneActivity za " +
            "LEFT JOIN za.zone z " +
            "LEFT JOIN tp.seatActivity sa " +
            "LEFT JOIN sa.seat s " +
            "WHERE o.account.accountId = :accountId AND o.status = :status AND tp.status = :tpStatus " +
            "ORDER BY o.orderDate DESC")
    List<MyTicketFlatDTO> findAllTicketInfoForAccountDESC(@Param("accountId") int accountId,
                                                      @Param("status") EOrderStatus status,
                                                      @Param("tpStatus") ETicketPurchaseStatus tpStatus
                                                      );
    @Query("SELECT new com.pse.tixclick.payload.dto.MyTicketFlatDTO(" +
            "o.orderId, o.orderCode, o.orderDate, o.totalAmount, o.totalAmountDiscount, " +
            "tp.ticketPurchaseId, o.qrCode, tp.quantity, t.price, t.ticketName, " +
            "e.eventId, e.eventName, e.logoURL, e.bannerURL, e.locationName, e.category.eventCategoryId, " +
            "e.address, e.ward, e.district, e.city, (e.seatMap IS NOT NULL), " +
            "ea.eventActivityId, ea.dateEvent, ea.startTimeEvent, " +
            "z.zoneName, s.seatName) " +
            "FROM Order o " +
            "JOIN o.orderDetails od " +
            "JOIN od.ticketPurchase tp " +
            "JOIN tp.ticket t " +
            "LEFT JOIN tp.event e " +
            "LEFT JOIN tp.eventActivity ea " +
            "LEFT JOIN tp.zoneActivity za " +
            "LEFT JOIN za.zone z " +
            "LEFT JOIN tp.seatActivity sa " +
            "LEFT JOIN sa.seat s " +
            "WHERE o.account.accountId = :accountId AND o.status = :status AND tp.status = :tpStatus " +
            "ORDER BY o.orderDate ASC")
    List<MyTicketFlatDTO> findAllTicketInfoForAccountASC(@Param("accountId") int accountId,
                                                      @Param("status") EOrderStatus status,
                                                      @Param("tpStatus") ETicketPurchaseStatus tpStatus
    );



    Optional<Order> findOrderByOrderCode(String orderCode);

    List<Order> findOrdersByStatus(EOrderStatus status);
}
