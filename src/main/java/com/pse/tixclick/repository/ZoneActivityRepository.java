package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.seatmap.Zone;
import com.pse.tixclick.payload.entity.seatmap.ZoneActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneActivityRepository extends JpaRepository<ZoneActivity, Integer> {
    @Query("SELECT z FROM ZoneActivity z WHERE z.eventActivity.eventActivityId = :id AND z.zone.zoneId = :zoneId")
    Optional<ZoneActivity> findByEventActivityIdAndZoneId(@Param("id") int id, @Param("zoneId") int zoneId);

    List<ZoneActivity> findZoneActivitiesByEventActivity_EventActivityId(int eventActivityId);

    @Query("SELECT z FROM ZoneActivity z WHERE z.zone.zoneId = :zoneId")
    List<ZoneActivity> findByZoneId(@Param("zoneId") int zoneId);

    List<ZoneActivity> findZoneActivitiesByEventActivity_EventActivityIdAndZone_Ticket_TicketId(int eventActivityId, int ticketId);
}
