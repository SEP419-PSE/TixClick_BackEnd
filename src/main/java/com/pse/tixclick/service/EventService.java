package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.request.CreateEventRequest;

public interface EventService {
    EventDTO createEvent(CreateEventRequest eventDTO);
}
