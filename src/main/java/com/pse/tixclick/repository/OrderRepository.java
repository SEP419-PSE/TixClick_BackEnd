package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.payment.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("select o from Order o where o.account.accountId = :id")
    List<Order> findByAccountId(@Param("id") int id);

    @Query("select o from Order o where o.account.accountId = :id and o.status = :status")
    List<Order> findByStatusOfAccount(@Param("id") int id, @Param("status") String status);
}
