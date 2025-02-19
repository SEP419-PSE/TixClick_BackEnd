package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.seatmap.Background;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface BackgroundRepository extends JpaRepository<Background,Integer> {
}
