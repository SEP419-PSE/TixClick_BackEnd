package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.seatmap.SeatMap;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatMapRepository extends JpaRepository<SeatMap,Integer> {
    @Query("select sm from SeatMap sm where sm.event.eventId = :eId")
    Optional<SeatMap> findSeatMapByEvent(@Param("eId") int eventId);
}
