package com.pse.tixclick.controller;

import com.pse.tixclick.payload.dto.SeatMapDTO;
import com.pse.tixclick.payload.dto.TicketPurchaseDTO;
import com.pse.tixclick.payload.request.create.CreateSeatMapRequest;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.OrderService;
import com.pse.tixclick.service.TicketPurchaseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticket-purchase")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TicketPurchaseController {
    @Autowired
    OrderService orderService;

    @Autowired
    TicketPurchaseService ticketPurchaseService;


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TicketPurchaseDTO>> createTicketPurchase(@RequestBody CreateTicketPurchaseRequest createTicketPurchaseRequest) {
        try {
            TicketPurchaseDTO ticketPurchaseDTO1 = ticketPurchaseService.createTicketPurchase(createTicketPurchaseRequest);
            return ResponseEntity.ok(
                    ApiResponse.<TicketPurchaseDTO>builder()
                            .code(200)
                            .message("Ticket Purchase created successfully")
                            .result(ticketPurchaseDTO1)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<TicketPurchaseDTO>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }
}
