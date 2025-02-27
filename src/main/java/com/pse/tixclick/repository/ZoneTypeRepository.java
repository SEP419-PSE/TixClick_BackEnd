package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.seatmap.ZoneType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ZoneTypeRepository extends JpaRepository<ZoneType, Integer> {
    @Query("SELECT z from ZoneType z where z.typeName = :name")
    ZoneType findZoneTypeByTypeName(String name);
}
