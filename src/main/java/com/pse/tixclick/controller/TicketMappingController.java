package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.TicketMappingDTO;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.TicketMappingResponse;
import com.pse.tixclick.service.TicketMappingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/ticket-mapping")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TicketMappingController {
    @Autowired
    TicketMappingService ticketMappingService;

    @GetMapping("/get-ticket-mapping-by-event-activity-id/{eventActivityId}")
    public ResponseEntity<ApiResponse<List<TicketMappingResponse>>> getTicketMappingByEventId(@PathVariable int eventActivityId) {
        try {
            List<TicketMappingResponse> ticketMappings = ticketMappingService.getAllTicketMappingByEventActivityId(eventActivityId);
            if (ticketMappings.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(
                        ApiResponse.<List<TicketMappingResponse>>builder()
                                .code(HttpStatus.OK.value())
                                .message("No ticket mappings found for this event")
                                .result(Collections.emptyList())
                                .build()
                );
            }
            return ResponseEntity.ok(
                    ApiResponse.<List<TicketMappingResponse>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Ticket mappings retrieved successfully")
                            .result(ticketMappings)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.<List<TicketMappingResponse>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }
}
