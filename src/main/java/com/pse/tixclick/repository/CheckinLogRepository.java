package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.CheckinLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckinLogRepository extends JpaRepository<CheckinLog, Integer> {
}
