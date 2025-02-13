package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.event.EventActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventActivityRepository extends JpaRepository<EventActivity,Integer> {
}
