package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.CheckinLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckinLogRepository extends JpaRepository<CheckinLog, Integer> {
    @Query(value = "SELECT COUNT(*) FROM CheckinLog WHERE checkinStatus = 'CHECKED_IN'")
    int countTotalCheckins();
}
