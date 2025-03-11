package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    @Query(value = "SELECT SUM(amount) FROM Transaction")
    Double sumTotalTransaction();

    @Query(value = """
        SELECT MONTH(o.order_date) AS month,
               COUNT(od.ticket_purchase_id) AS total_tickets_sold,
               COALESCE(SUM(o.total_amount), 0) AS total_revenue
        FROM orders o
        JOIN order_detail od ON o.order_id = od.order_id
        JOIN ticket_purchase tp ON od.ticket_purchase_id = tp.ticket_purchase_id
        WHERE YEAR(o.order_date) = YEAR(GETDATE()) 
          AND tp.status = 'PURCHASED'
        GROUP BY MONTH(o.order_date)
        ORDER BY month
    """, nativeQuery = true)
    List<Object[]> getMonthlySalesReport();
}
