package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.seatmap.SeatActivity;
import com.pse.tixclick.payload.entity.seatmap.ZoneActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatActivityRepository extends JpaRepository<SeatActivity, Integer> {
    @Query("SELECT s FROM SeatActivity s WHERE s.eventActivity.eventActivityId = :id AND s.seat.seatId = :seatId")
    Optional<SeatActivity> findByEventActivityIdAndSeatId(@Param("id") int id, @Param("seatId") int seatId);

    List<SeatActivity> findSeatActivitiesByZoneActivity_ZoneActivityId(int zoneActivityId);
}
