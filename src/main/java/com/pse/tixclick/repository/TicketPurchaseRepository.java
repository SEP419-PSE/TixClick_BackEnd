package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketPurchaseRepository extends JpaRepository<TicketPurchase, Integer> {
}
