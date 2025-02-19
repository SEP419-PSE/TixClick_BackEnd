package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.seatmap.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
}
