package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TicketPurchaseRepository extends JpaRepository<TicketPurchase, Integer> {
}
