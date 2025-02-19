package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.request.create.CreateEventRequest;
import com.pse.tixclick.payload.request.update.UpdateEventRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/event")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {
    @Autowired
    private EventService eventService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<EventDTO>> createEvent(
            @ModelAttribute CreateEventRequest eventDTO,
            @RequestParam("logoURL") MultipartFile logoURL,
            @RequestParam("bannerURL") MultipartFile bannerURL
            ) {
        try {
            EventDTO createdEvent = eventService.createEvent(eventDTO, logoURL, bannerURL);

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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<EventDTO>> updateEvent(
            @ModelAttribute UpdateEventRequest eventDTO,
            @RequestParam("logoURL") MultipartFile logoURL,
            @RequestParam("bannerURL") MultipartFile bannerURL
            ) {
        try {
            EventDTO updatedEvent = eventService.updateEvent(eventDTO, logoURL, bannerURL);

            ApiResponse<EventDTO> response = ApiResponse.<EventDTO>builder()
                    .code(HttpStatus.OK.value())
                    .message("Event updated successfully")
                    .result(updatedEvent)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (AppException e) {
            ApiResponse<EventDTO> errorResponse = ApiResponse.<EventDTO>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Lỗi từ service
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteEvent(@PathVariable int id) {
        try {
            boolean isDeleted = eventService.deleteEvent(id);

            ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                    .code(HttpStatus.OK.value())
                    .message("Event deleted successfully")
                    .result(isDeleted)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (AppException e) {
            ApiResponse<Boolean> errorResponse = ApiResponse.<Boolean>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage()) // Lỗi từ service
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<EventDTO>>> getAllEvent() {
        List<EventDTO> events = eventService.getAllEvent();

        if (events.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<EventDTO>>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message("No events found")
                            .result(null)
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<List<EventDTO>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get all events successfully")
                        .result(events)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDTO>> getEventById(@PathVariable int id) {
        try {
            EventDTO event = eventService.getEventById(id);
            return ResponseEntity.ok(
                    ApiResponse.<EventDTO>builder()
                            .code(HttpStatus.OK.value())
                            .message("Get event by id successfully")
                            .result(event)
                            .build()
            );
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<EventDTO>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message("Event not found with id: " + id)
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<EventDTO>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("An error occurred while retrieving the event")
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<EventDTO>>> getEventByStatus(@PathVariable String status) {
        List<EventDTO> events = eventService.getEventByStatus(status);

        if (events.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<EventDTO>>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message("No events found with status: " + status)
                            .result(null)
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<List<EventDTO>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get all events with status: " + status + " successfully")
                        .result(events)
                        .build()
        );
    }

    @GetMapping("/draft")
    public ResponseEntity<ApiResponse<List<EventDTO>>> getEventByDraft() {
        List<EventDTO> events = eventService.getEventByDraft();

        if (events.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<EventDTO>>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message("No draft events found")
                            .result(null)
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<List<EventDTO>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get all draft events successfully")
                        .result(events)
                        .build()
        );
    }

    @GetMapping("/completed")
    public ResponseEntity<ApiResponse<List<EventDTO>>> getEventByCompleted() {
        List<EventDTO> events = eventService.getEventByCompleted();

        if (events.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<EventDTO>>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message("No completed events found")
                            .result(null)
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<List<EventDTO>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get all completed events successfully")
                        .result(events)
                        .build()
        );
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<EventDTO>> approveEvent(@PathVariable int id) {
        try {
            EventDTO event = eventService.approveEvent(id);
            return ResponseEntity.ok(
                    ApiResponse.<EventDTO>builder()
                            .code(HttpStatus.OK.value())
                            .message("Event approved successfully")
                            .result(event)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<EventDTO>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/account")
    public ResponseEntity<ApiResponse<List<EventDTO>>> getAllEventsByAccountId() {
        List<EventDTO> events = eventService.getAllEventsByAccountId();

        if (events.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<EventDTO>>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message("No events found")
                            .result(null)
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<List<EventDTO>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get all events successfully")
                        .result(events)
                        .build()
        );
    }

    @GetMapping("/account/{status}")
    public ResponseEntity<ApiResponse<List<EventDTO>>> getEventsByAccountIdAndStatus(@PathVariable String status) {
        List<EventDTO> events = eventService.getEventsByAccountIdAndStatus(status);

        if (events.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<EventDTO>>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message("No events found with status: " + status)
                            .result(null)
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<List<EventDTO>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get all events with status: " + status + " successfully")
                        .result(events)
                        .build()
        );
    }
}
