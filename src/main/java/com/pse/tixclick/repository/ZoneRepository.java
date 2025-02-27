package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.seatmap.Zone;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Integer> {
    @Query("SELECT z from Zone z where z.seatMap.seatMapId = :seatMapId")
    List<Zone> findBySeatMapId(@Param("seatMapId") int seatMapId);

    @Query("SELECT z from Zone z where z.zoneType.typeId = :type")
    List<Zone> findByZoneType(@Param("type") int type);
}
