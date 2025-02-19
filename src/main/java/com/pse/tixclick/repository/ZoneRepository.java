package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.seatmap.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Integer> {
}
