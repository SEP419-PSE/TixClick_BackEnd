package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,Integer> {
    Optional<Ticket> findTicketByTicketCode(String ticketCode);
}
