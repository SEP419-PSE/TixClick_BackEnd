package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.dto.UpcomingEventDTO;
import com.pse.tixclick.payload.entity.entity_enum.EEventStatus;
import com.pse.tixclick.payload.request.create.CreateEventRequest;
import com.pse.tixclick.payload.request.update.UpdateEventRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.EventDetailForConsumer;
import com.pse.tixclick.payload.response.EventForConsumerResponse;
import com.pse.tixclick.payload.response.EventResponse;
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
import java.util.Map;
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
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvent() {
        List<EventResponse> events = eventService.getAllEvent();

        if (events.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<EventResponse>>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message("No events found")
                            .result(null)
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<List<EventResponse>>builder()
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
    public ResponseEntity<ApiResponse<List<EventDTO>>> getEventByStatus(@PathVariable EEventStatus status) {
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
    public ResponseEntity<ApiResponse<EventDTO>> approveEvent(@PathVariable int id, @RequestBody EEventStatus status) {
        try {
            EventDTO event = eventService.approveEvent(id,status);
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

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<EventDTO>>> getEventsByCompanyId(@PathVariable int companyId) {
        List<EventDTO> events = eventService.getEventsByCompanyId(companyId);

        if (events.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<EventDTO>>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message("No events found with company id: " + companyId)
                            .result(null)
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<List<EventDTO>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get all events with company id: " + companyId + " successfully")
                        .result(events)
                        .build()
        );
    }

    @GetMapping("/count/scheduled")
    public ResponseEntity<ApiResponse<Integer>> countTotalScheduledEvents() {
        try {
            int count = eventService.countTotalScheduledEvents();
            return ResponseEntity.ok(
                    ApiResponse.<Integer>builder()
                            .code(200)
                            .message("Total Upcoming Events")
                            .result(count)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<Integer>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/average/price")
    public ResponseEntity<ApiResponse<Double>> getAverageTicketPrice() {
        try {
            double average = eventService.getAverageTicketPrice();
            return ResponseEntity.ok(
                    ApiResponse.<Double>builder()
                            .code(200)
                            .message("Average Ticket Price")
                            .result(average)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<Double>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/category/distribution")
    public ResponseEntity<Map<String, Double>> getEventCategoryDistribution() {
        return ResponseEntity.ok(eventService.getEventCategoryDistribution());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<UpcomingEventDTO>>> getUpcomingEvents() {
        List<UpcomingEventDTO> events = eventService.getUpcomingEvents();

        if (events.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<UpcomingEventDTO>>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message("No upcoming events found")
                            .result(null)
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<List<UpcomingEventDTO>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get all upcoming events successfully")
                        .result(events)
                        .build()
        );
    }

    @GetMapping("/top-performing")
    public ResponseEntity<ApiResponse<List<UpcomingEventDTO>>> getTopPerformingEvents() {
        List<UpcomingEventDTO> events = eventService.getTopPerformingEvents();

        if (events.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<UpcomingEventDTO>>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message("No top performing events found")
                            .result(null)
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<List<UpcomingEventDTO>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get all top performing events successfully")
                        .result(events)
                        .build()
        );
    }

    @GetMapping("/request-approval/{eventId}")
    public ResponseEntity<ApiResponse<String>> sentRequestForApproval(@PathVariable int eventId) {
        try {
            String message = eventService.sentRequestForApproval(eventId);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .code(HttpStatus.OK.value())
                            .message(message)
                            .result(message)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<String>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<String>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("An error occurred while sending request for approval")
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/consumer/scheduled")
    public ResponseEntity<ApiResponse<List<EventForConsumerResponse>>> getEventsForConsumerByStatusScheduled() {
        List<EventForConsumerResponse> events = eventService.getEventsForConsumerByStatusScheduled();

        if (events.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<List<EventForConsumerResponse>>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message("No scheduled events found")
                            .result(null)
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<List<EventForConsumerResponse>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get all scheduled events successfully")
                        .result(events)
                        .build()
        );
    }


    @GetMapping("/consumer/{eventId}")
    public ResponseEntity<ApiResponse<EventDetailForConsumer>> getEventDetailForConsumer(@PathVariable int eventId) {
        try {
            EventDetailForConsumer event = eventService.getEventDetailForConsumer(eventId);
            return ResponseEntity.ok(
                    ApiResponse.<EventDetailForConsumer>builder()
                            .code(HttpStatus.OK.value())
                            .message("Get event detail for consumer successfully")
                            .result(event)
                            .build()
            );
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<EventDetailForConsumer>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message("Event not found with id: " + eventId)
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<EventDetailForConsumer>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("An error occurred while retrieving the event detail for consumer")
                            .result(null)
                            .build());
        }
    }
}
