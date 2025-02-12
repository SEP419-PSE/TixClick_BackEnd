package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.request.CreateEventRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/event")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {
    @Autowired
    private EventService eventService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<EventDTO>> createEvent(@RequestBody CreateEventRequest eventDTO) {
        try {
            EventDTO createdEvent = eventService.createEvent(eventDTO);

            ApiResponse<EventDTO> response = ApiResponse.<EventDTO>builder()
                    .code(HttpStatus.CREATED.value())
                    .message("Event created successfully")
                    .result(createdEvent)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (AppException e) {
            ApiResponse<EventDTO> errorResponse = ApiResponse.<EventDTO>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Lỗi từ service
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

}
