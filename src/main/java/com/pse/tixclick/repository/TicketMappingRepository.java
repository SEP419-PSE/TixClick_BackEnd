package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.ticket.TicketMapping;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TicketMappingRepository extends JpaRepository<TicketMapping, Integer> {
    @Query(value = "SELECT tm FROM TicketMapping tm WHERE tm.ticket.ticketId = :ticketId AND tm.eventActivity.eventActivityId = :eventActivityId")
    Optional<TicketMapping> findTicketMappingByTicketIdAndEventActivityId(@Param("ticketId") int ticketId, @Param("eventActivityId") int eventActivityId);

    List<TicketMapping> findTicketMappingsByEventActivity_EventActivityId(int eventActivityId);

    List<TicketMapping> findTicketMappingsByEventActivity_Event(Event event);

    List<TicketMapping> findTicketMappingsByEventActivity_EventActivityIdAndTicket_TicketIdAndQuantity(int eventActivityId, int ticketId, int quantity);
}
