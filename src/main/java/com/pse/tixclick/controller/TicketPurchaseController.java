package com.pse.tixclick.controller;

import com.pse.tixclick.payload.dto.*;
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

import java.util.Collections;
import java.util.List;

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

    @GetMapping("/count_ticket_sold")
    public ResponseEntity<ApiResponse<Integer>> countTotalTicketSold() {
        try {
            int count = ticketPurchaseService.countTotalTicketSold();
            return ResponseEntity.ok(
                    ApiResponse.<Integer>builder()
                            .code(200)
                            .message("Total ticket sold")
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

    @GetMapping("/monthly_ticket_sales")
    public ResponseEntity<ApiResponse> getMonthlyTicketSales() {
        try {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .code(200)
                            .message("Monthly Ticket Sales")
                            .result(ticketPurchaseService.getMonthlyTicketSales())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @PostMapping("/checkin/{checkinId}")
    public ResponseEntity<ApiResponse<String>> checkinTicketPurchase(@PathVariable int checkinId) {
        try {
            String checkin = ticketPurchaseService.checkinTicketPurchase(checkinId);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .code(200)
                            .message(checkin)
                            .result(checkin)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<String>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/count_checkins")
    public ResponseEntity<ApiResponse<Integer>> countTotalCheckins() {
        try {
            int count = ticketPurchaseService.countTotalCheckins();
            return ResponseEntity.ok(
                    ApiResponse.<Integer>builder()
                            .code(200)
                            .message("Total checkins")
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

    @GetMapping("/tickets_sold_revenue_by_day/{day}")
    public ResponseEntity<ApiResponse> getTicketsSoldAndRevenueByDay(@PathVariable int day) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .code(200)
                            .message("Tickets sold and revenue by day")
                            .result(ticketPurchaseService.getTicketsSoldAndRevenueByDay(day))
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/all_of_account")
    public ResponseEntity<ApiResponse<List<MyTicketDTO>>> getAllTicketPurchaseByAccount() {
        try {
            List<MyTicketDTO> myTicketDTOS = ticketPurchaseService.getTicketPurchasesByAccount();

            if (myTicketDTOS == null || myTicketDTOS.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.<List<MyTicketDTO>>builder()
                                .code(404)
                                .message("No ticket purchases found")
                                .result(Collections.emptyList()) // Trả về danh sách rỗng thay vì null
                                .build());
            }

            return ResponseEntity.ok(
                    ApiResponse.<List<MyTicketDTO>>builder()
                            .code(200)
                            .message("Successfully fetched all Ticket Purchase by account")
                            .result(myTicketDTOS)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<List<MyTicketDTO>>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(Collections.emptyList()) // Trả về danh sách rỗng thay vì null
                            .build());
        }
    }

    @PostMapping("/decrypt_qr_code")
    public ResponseEntity<ApiResponse<TicketQrCodeDTO>> decryptQrCode(@RequestBody String qrCode) throws Exception {
        TicketQrCodeDTO qr = ticketPurchaseService.decryptQrCode(qrCode);
        return ResponseEntity.ok(
                ApiResponse.<TicketQrCodeDTO>builder()
                        .code(200)
                        .message("Successfully decrypted qr code")
                        .result(qr)
                        .build()
        );
    }
}
