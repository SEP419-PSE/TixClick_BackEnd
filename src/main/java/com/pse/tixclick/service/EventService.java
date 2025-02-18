package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.request.CreateEventRequest;
import com.pse.tixclick.payload.request.UpdateEventRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface EventService {
    EventDTO createEvent(CreateEventRequest eventDTO, MultipartFile logoURL, MultipartFile bannerURL) throws IOException;

    EventDTO updateEvent(UpdateEventRequest eventRequest, MultipartFile logoURL, MultipartFile bannerURL) throws IOException;

    boolean deleteEvent(int id);

    List<EventDTO> getAllEvent();

    EventDTO getEventById(int id);

    List<EventDTO> getEventByStatus(String status);

    List<EventDTO> getEventByDraft();

    List<EventDTO> getEventByCompleted();

    EventDTO approveEvent(int id);
}
