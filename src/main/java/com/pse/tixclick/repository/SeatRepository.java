package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.seatmap.Seat;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    @Query("select s from Seat s where s.zone.zoneId = :zoneId")
    List<Seat> getSeatsByZoneId(@Param("zoneId") int zoneId);

    @Query("select s from Seat s where s.zone.seatMap.seatMapId = :seatMapId")
    List<Seat> getSeatsBySeatMapId(@Param("seatMapId") int seatMapId);

}
