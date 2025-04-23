package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.EventActivityDTO;
import com.pse.tixclick.payload.request.CreateEventActivityAndTicketRequest;
import com.pse.tixclick.payload.request.create.CreateEventActivityRequest;

import java.util.List;

public interface   EventActivityService {
    EventActivityDTO createEventActivity(CreateEventActivityRequest eventActivityRequest);

    EventActivityDTO updateEventActivity(CreateEventActivityRequest eventActivityRequest, int id);

    boolean deleteEventActivity(int id);

    List<EventActivityDTO> getEventActivityByEventId(int eventId);

    List<CreateEventActivityAndTicketRequest> createEventActivityAndTicket(List<CreateEventActivityAndTicketRequest> request, String contractCode);

    List<CreateEventActivityAndTicketRequest> getEventActivityAndTicketByEventId(int eventId);

}
