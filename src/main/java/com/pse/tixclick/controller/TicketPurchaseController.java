package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.*;
import com.pse.tixclick.payload.request.create.CreateSeatMapRequest;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;
import com.pse.tixclick.payload.request.create.ListTicketPurchaseRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.OrderService;
import com.pse.tixclick.service.TicketPurchaseService;
import com.pse.tixclick.service.TransactionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

    @Autowired
    TransactionService transactionService;


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<List<TicketPurchaseDTO>>> createTicketPurchase(@RequestBody ListTicketPurchaseRequest createTicketPurchaseRequest) {
        try {
            List<TicketPurchaseDTO> ticketPurchaseDTO1 = ticketPurchaseService.createTicketPurchase(createTicketPurchaseRequest);
            ApiResponse<List<TicketPurchaseDTO>> response = ApiResponse.<List<TicketPurchaseDTO>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Ticket Purchase created successfully")
                            .result(ticketPurchaseDTO1)
                            .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (AppException e) {
            ApiResponse<List<TicketPurchaseDTO>> errorResponse = ApiResponse.<List<TicketPurchaseDTO>>builder()                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .result(null)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/count_ticket_sold")
    public ResponseEntity<ApiResponse<Integer>> countTotalTicketSold() {
        try {
            int count = ticketPurchaseService.countTotalTicketSold();
            return ResponseEntity.ok(
                    ApiResponse.<Integer>builder()
                            .code(HttpStatus.OK.value())
                            .message("Total ticket sold")
                            .result(count)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<Integer>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
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
    public ResponseEntity<ApiResponse<TicketQrCodeDTO>> decryptQrCode(@RequestBody String qrCode) {
        try {
            TicketQrCodeDTO qr = ticketPurchaseService.decryptQrCode(qrCode);
            ApiResponse<TicketQrCodeDTO> response = ApiResponse.<TicketQrCodeDTO>builder()
                    .code(HttpStatus.OK.value())
                    .message("Successfully decrypted QR code")
                    .result(qr)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (AppException e) {
            ApiResponse<TicketQrCodeDTO> errorResponse = ApiResponse.<TicketQrCodeDTO>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .result(null)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Object>> total() {
        int ticket = ticketPurchaseService.countTicketPurchaseStatusByPurchased();
        Double totalTransaction = transactionService.totalTransaction();

        Map<String, Object> response = new HashMap<>();
        response.put("totalTickets", ticket);
        response.put("totalTransaction", totalTransaction);

        return ResponseEntity.ok(
                ApiResponse.<Object>builder()
                        .code(200)
                        .message("Overview statistics")
                        .result(response)
                        .build()
        );
    }

    @GetMapping("/print_active_threads")
    public ResponseEntity<ApiResponse<Integer>> printActiveThreads() {
        int activeThreads = ticketPurchaseService.printActiveThreads();
        return ResponseEntity.ok(
                ApiResponse.<Integer>builder()
                        .code(200)
                        .message("Active threads count")
                        .result(activeThreads)
                        .build()
        );
    }
}
