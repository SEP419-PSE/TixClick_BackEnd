package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.entity_enum.ZoneTypeEnum;
import com.pse.tixclick.payload.entity.seatmap.ZoneType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ZoneTypeRepository extends JpaRepository<ZoneType, Integer> {
    Optional<ZoneType> findZoneTypeByTypeName(ZoneTypeEnum name);

}
