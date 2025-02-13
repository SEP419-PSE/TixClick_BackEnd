package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.request.CreateEventRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface EventService {
    EventDTO createEvent(CreateEventRequest eventDTO, MultipartFile logoURL, MultipartFile bannerURL, MultipartFile logoOrganizeURL) throws IOException;
}
