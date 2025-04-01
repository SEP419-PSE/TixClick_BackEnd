package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    Optional<Ticket> findTicketByTicketCode(String ticketCode);

    List<Ticket> findTicketsByEvent_EventId(int eventId);

    @Query(value = "SELECT TOP 1 * \n" +
            "FROM ticket \n" +
            "WHERE event_id = :eventId \n" +
            "ORDER BY price ASC;\n", nativeQuery = true)
    Optional<Ticket> findMinTicketByEvent_EventId(int eventId);
}
