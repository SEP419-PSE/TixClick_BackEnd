package com.pse.tixclick.startup;

import com.pse.tixclick.payload.entity.entity_enum.EEventStatus;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.repository.EventActivityRepository;
import com.pse.tixclick.repository.EventRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventStatusScheduler {
    EventRepository eventRepository;
    EventActivityRepository eventActivityRepository;

    @Scheduled(fixedRate = 60 * 1000)
    public void updateEventStatus() {
        List<Event> events = eventRepository.findEventsByStatus(EEventStatus.SCHEDULED);
        if(events.isEmpty()){
            System.out.println("No events found with status SCHEDULED");
            return;
        }

        for (Event event : events) {
            List<EventActivity> activities = eventActivityRepository.findEventActivitiesByEvent_EventId(event.getEventId());
            if (activities.isEmpty()) {
                System.out.println("No activities found for event: " + event.getEventId());
                return;
            }
            LocalDateTime latestEndDateTime = activities.stream()
                    .map(a -> a.getDateEvent().atTime(a.getEndTimeEvent()))
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

            if (latestEndDateTime != null && latestEndDateTime.isBefore(LocalDateTime.now())) {
                // Cập nhật status nếu đã kết thúc
                event.setStatus(EEventStatus.ENDED);
                eventRepository.save(event);
                System.out.println("Event ID " + event.getEventId() + " is now ENDED.");
            }
        }
    }
}
