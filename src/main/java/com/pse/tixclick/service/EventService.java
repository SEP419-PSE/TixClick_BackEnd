package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.dto.UpcomingEventDTO;
import com.pse.tixclick.payload.entity.entity_enum.EEventStatus;
import com.pse.tixclick.payload.request.create.CreateEventRequest;
import com.pse.tixclick.payload.request.update.UpdateEventRequest;
import com.pse.tixclick.payload.response.EventResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface EventService {
    EventDTO createEvent(CreateEventRequest eventDTO, MultipartFile logoURL, MultipartFile bannerURL) throws IOException;

    EventDTO updateEvent(UpdateEventRequest eventRequest, MultipartFile logoURL, MultipartFile bannerURL) throws IOException;

    boolean deleteEvent(int id);

    List<EventResponse> getAllEvent();

    EventDTO getEventById(int id);

    List<EventDTO> getEventByStatus(String status);

    List<EventDTO> getEventByDraft();

    List<EventDTO> getEventByCompleted();

    EventDTO approveEvent(int id, EEventStatus status);

    List<EventDTO> getAllEventsByAccountId();

    List<EventDTO> getEventsByAccountIdAndStatus(String status);

    List<EventDTO> getEventsByCompanyId(int companyId);

    int countTotalScheduledEvents();

    double getAverageTicketPrice();

    Map<String, Double> getEventCategoryDistribution();

    List<UpcomingEventDTO> getUpcomingEvents();

    List<UpcomingEventDTO> getTopPerformingEvents();



}
