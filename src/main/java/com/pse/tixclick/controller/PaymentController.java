package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.OrderDTO;
import com.pse.tixclick.payload.dto.PaymentDTO;
import com.pse.tixclick.payload.dto.TicketPurchaseDTO;
import com.pse.tixclick.payload.dto.TicketQrCodeDTO;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.request.ChangeTicketRequest;
import com.pse.tixclick.payload.request.TicketPurchaseRequest;
import com.pse.tixclick.payload.request.create.CreateContractAndDetailRequest;
import com.pse.tixclick.payload.request.create.CreateOrderRequest;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.PayOSResponse;
import com.pse.tixclick.payload.response.PaymentResponse;
import com.pse.tixclick.payload.response.ResponseObject;
import com.pse.tixclick.repository.EventRepository;
import com.pse.tixclick.service.OrderService;
import com.pse.tixclick.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/payment")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    @Autowired
    OrderService orderService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    EventRepository eventRepository;

    @PostMapping("/pay-os-create")
    public ResponseEntity<ApiResponse<PayOSResponse>> payOS(@RequestBody CreateOrderRequest request, HttpServletRequest httpServletRequest) {
        try {
            OrderDTO orderDTO = orderService.createOrder(request);
            PayOSResponse payOSResponse = paymentService.createPaymentLink(orderDTO.getOrderId(), orderDTO.getVoucherCode(), request.getExpiredTime(), httpServletRequest);

            ApiResponse<PayOSResponse> response = ApiResponse.<PayOSResponse>builder()
                    .code(HttpStatus.OK.value())
                    .message("Success")
                    .result(payOSResponse)
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<PayOSResponse> errorResponse = ApiResponse.<PayOSResponse>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .result(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


    @GetMapping("/payos_call_back")
    public void payOSCallbackHandler(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String mainUrl = "http://localhost:5173/payment/queue";
        String fallbackUrl = "http://localhost:5173/payment/queue"; // hoặc URL khác nếu cần

        PaymentResponse payment = paymentService.handleCallbackPayOS(request);

        String redirectUrl = mainUrl;
        if (!"00".equals(payment.getCode())) {
            redirectUrl = mainUrl; // vẫn là mainUrl nếu thất bại, nhưng vẫn xử lý fallback bên dưới
        }

        try {
            // Redirect đến mainUrl
            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            // Nếu không redirect được (ví dụ: timeout, domain lỗi...), fallback về localhost
            System.err.println("Redirect đến mainUrl thất bại, thử lại với fallback: " + e.getMessage());
            response.sendRedirect(fallbackUrl);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getAllPayments() {
        try {
            List<PaymentDTO> paymentDTOS = paymentService.getAllPayments();
            return ResponseEntity.ok(
                    ApiResponse.<List<PaymentDTO>>builder()
                            .code(200)
                            .message("Successfully fetched all payments")
                            .result(paymentDTOS)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<List<PaymentDTO>>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @PostMapping("/test-qr")
    public ResponseEntity<ApiResponse<String>> testQR(@RequestBody TicketQrCodeDTO ticketQrCodeDTO) {
        try {
            String qr = paymentService.testQR(ticketQrCodeDTO);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .code(200)
                            .message("Successfully fetched all payments")
                            .result(qr)
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

    @PostMapping("/change-ticket")
    @Operation(summary = "Change existing tickets", description = "Changes tickets based on provided ticket purchase requests and change details")
    public ResponseEntity<ApiResponse<PayOSResponse>> changeTicket(
            @RequestBody ChangeTicketRequest changeTicketRequest,
            HttpServletRequest request) {
        try {
            PayOSResponse result = paymentService.changTicket(
                    changeTicketRequest.getTicketPurchaseRequests(),
                    changeTicketRequest.getTicketChange(),
                    changeTicketRequest.getOrderCode(),
                    request
            );
            return ResponseEntity.ok(
                    ApiResponse.<PayOSResponse>builder()
                            .code(HttpStatus.OK.value())
                            .message("Successfully changed ticket")
                            .result(result)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<PayOSResponse>builder()
                            .code(e.getErrorCode().getCode())
                            .message(e.getMessage())
                            .result(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<PayOSResponse>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Unexpected error: " + e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/export_refund/{eventId}")
    public void exportRefunds(@PathVariable int eventId, HttpServletResponse response) throws IOException {
        /* 1. Danh sách cột cố định */
        List<String> columnList = List.of(
                "transactionCode",
                "transactionDate",
                "orderCode",
                "price",
                "userName",
                "email",
                "phone",
                "status"
        );

        Event event = eventRepository.findEvent(eventId);

        /* 2. Header tải file */
        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String filename = URLEncoder.encode(
                "refunds_" + event.getEventCode() + "_" + LocalDate.now() + ".xlsx", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        /* 3. Gọi service – ghi trực tiếp ra OutputStream */
        paymentService.exportRefunds(columnList, response.getOutputStream(), eventId);
    }

    @PostMapping("/import_refund")
    public ResponseEntity<ApiResponse<String>> importRefund(@RequestParam("file") MultipartFile file) {
        try {
            String result = paymentService.readOrderCodeAndStatus(file.getInputStream());
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .code(HttpStatus.OK.value())
                    .result(result)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .code(HttpStatus.OK.value())
                    .message(e.getMessage())
                    .result(null)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }
}
