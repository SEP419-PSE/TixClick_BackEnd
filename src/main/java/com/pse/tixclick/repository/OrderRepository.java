package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

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
}
