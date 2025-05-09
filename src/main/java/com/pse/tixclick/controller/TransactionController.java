package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.TicketQrCodeDTO;
import com.pse.tixclick.payload.dto.TransactionCompanyByEventDTO;
import com.pse.tixclick.payload.dto.TransactionDTO;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.PaginationResponse;
import com.pse.tixclick.service.TransactionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionController {
    @Autowired
    TransactionService transactionService;

    @GetMapping("/sum_total_transaction")
    public ResponseEntity<ApiResponse<Double>> sumTotalTransaction() {
        try {
            double sum = transactionService.sumTotalTransaction();
            return ResponseEntity.ok(
                    ApiResponse.<Double>builder()
                            .code(200)
                            .message("Total Revenue")
                            .result(sum)
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

    @GetMapping("/monthly_sales_report")
    public ResponseEntity<ApiResponse> getMonthlySalesReport() {
        try {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .code(200)
                            .message("Monthly Sales Report")
                            .result(transactionService.getMonthlySalesReport())
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

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactions() {
        try {
            List<TransactionDTO> transactionDTOS = transactionService.getTransactions();
            return ResponseEntity.ok(
                    ApiResponse.<List<TransactionDTO>>builder()
                            .code(200)
                            .message("All Transactions")
                            .result(transactionDTOS)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<List<TransactionDTO>>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/total_transaction_company/{eventId}")
    public ResponseEntity<ApiResponse<PaginationResponse<TransactionCompanyByEventDTO>>> getTransactionsByEvent(
            @PathVariable int eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            int size = 6;
            PaginationResponse<TransactionCompanyByEventDTO> pagination = transactionService
                    .getTransactionCompanyByEvent(eventId, page, size, sortDirection);

            ApiResponse<PaginationResponse<TransactionCompanyByEventDTO>> response =
                    ApiResponse.<PaginationResponse<TransactionCompanyByEventDTO>>builder()
                            .code(HttpStatus.OK.value())
                            .message("Successfully fetched transactions by event")
                            .result(pagination)
                            .build();

            return ResponseEntity.ok(response);

        } catch (AppException e) {
            ApiResponse<PaginationResponse<TransactionCompanyByEventDTO>> errorResponse =
                    ApiResponse.<PaginationResponse<TransactionCompanyByEventDTO>>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .result(null)
                            .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

}
