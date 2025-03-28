package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.ticket.TicketMapping;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface TicketMappingRepository extends JpaRepository<TicketMapping, Integer> {
    @Query(value = "SELECT tm FROM TicketMapping tm WHERE tm.ticket.ticketId = :ticketId AND tm.eventActivity.eventActivityId = :eventActivityId")
    Optional<TicketMapping> findTicketMappingByTicketIdAndEventActivityId(@Param("ticketId") int ticketId, @Param("eventActivityId") int eventActivityId);
}
