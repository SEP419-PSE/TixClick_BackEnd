package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event,Integer> {
    Optional<Event> findEventByEventId(int eventId);


}
