package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.event.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventImageRepository extends JpaRepository<EventImage,Integer> {
}
