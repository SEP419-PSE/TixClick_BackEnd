package com.pse.tixclick.repository;

import com.pse.tixclick.payload.entity.event.EventActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventActivityRepository extends JpaRepository<EventActivity,Integer> {
    List<EventActivity> findEventActivitiesByEvent_EventId(int eventId);

    Optional<EventActivity> findEventActivityByActivityName(String activityName);
}
