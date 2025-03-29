package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.TicketDTO;
import com.pse.tixclick.payload.request.TicketRequest;
import com.pse.tixclick.payload.request.create.CreateTickeSeatMaptRequest;
import com.pse.tixclick.payload.request.create.CreateTicketRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.TicketService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/ticket")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TicketController {
    TicketService ticketService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TicketDTO>> createTicket(@RequestBody CreateTicketRequest ticketDTO) {
        try {
            TicketDTO ticket = ticketService.createTicket(ticketDTO);
            return ResponseEntity.ok(
                    ApiResponse.<TicketDTO>builder()
                            .code(HttpStatus.OK.value())
                            .message("Ticket created successfully")
                            .result(ticket)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(ApiResponse.<TicketDTO>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/get-tickets-by-event-id/{eventId}")
    public ResponseEntity<ApiResponse<List<TicketRequest>>> getTicketsByEventId(@PathVariable int eventId) {
        try {
            List<TicketRequest> tickets = ticketService.getAllTicketByEventId(eventId);
            if (tickets.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.<List<TicketRequest>>builder()
                                .code(HttpStatus.NOT_FOUND.value())
                                .message("No tickets found for this event")
                                .result(Collections.emptyList())
                                .build()
                );
            }
            return ResponseEntity.ok(
                    ApiResponse.<List<TicketRequest>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Tickets retrieved successfully")
                            .result(tickets)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<List<TicketRequest>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<TicketRequest>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Error retrieving tickets: " + e.getMessage())
                            .result(null)
                            .build()
            );
        }
    }

    @DeleteMapping("/delete/{ticketCode}")
    public ResponseEntity<ApiResponse<List<TicketRequest>>> deleteTicket(@PathVariable String ticketCode) {
        try {
            List<TicketRequest> tickets = ticketService.deleteTicket(ticketCode);
            return ResponseEntity.ok(
                    ApiResponse.<List<TicketRequest>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Ticket deleted successfully")
                            .result(tickets)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<List<TicketRequest>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<TicketRequest>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Error deleting ticket: " + e.getMessage())
                            .result(null)
                            .build()
            );
        }
    }

    @PostMapping("/create-ticket-seat-map")
    public ResponseEntity<ApiResponse<List<TicketRequest>>> createTicketSeatMap(@RequestBody CreateTickeSeatMaptRequest request) {
        try {
            List<TicketRequest> tickets = ticketService.createTicketSeatMap(request);
            return ResponseEntity.ok(
                    ApiResponse.<List<TicketRequest>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Ticket seat map created successfully")
                            .result(tickets)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<List<TicketRequest>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<TicketRequest>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Error creating ticket seat map: " + e.getMessage())
                            .result(null)
                            .build()
            );
        }
    }

    @PutMapping("/update-ticket-seat-map")
    public ResponseEntity<ApiResponse<List<TicketRequest>>> updateTicketSeatMap(@RequestBody CreateTickeSeatMaptRequest request) {
        try {
            List<TicketRequest> tickets = ticketService.updateTicketSeatMap(request);
            return ResponseEntity.ok(
                    ApiResponse.<List<TicketRequest>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Ticket seat map updated successfully")
                            .result(tickets)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<List<TicketRequest>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<TicketRequest>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Error updating ticket seat map: " + e.getMessage())
                            .result(null)
                            .build()
            );
        }
    }
}
