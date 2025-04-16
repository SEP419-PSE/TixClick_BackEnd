package com.pse.tixclick.controller;

import com.pse.tixclick.payload.dto.OrderDTO;
import com.pse.tixclick.payload.dto.PaymentDTO;
import com.pse.tixclick.payload.dto.TicketQrCodeDTO;
import com.pse.tixclick.payload.request.create.CreateOrderRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.PayOSResponse;
import com.pse.tixclick.payload.response.PaymentResponse;
import com.pse.tixclick.payload.response.ResponseObject;
import com.pse.tixclick.service.OrderService;
import com.pse.tixclick.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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


    @PostMapping("/pay-os-create")
    public ResponseEntity<ApiResponse<PayOSResponse>> payOS(@RequestBody CreateOrderRequest request, HttpServletRequest httpServletRequest) {
        try {
            OrderDTO orderDTO = orderService.createOrder(request);
            PayOSResponse payOSResponse = paymentService.createPaymentLink(orderDTO.getOrderId(), request.getExpiredTime(), httpServletRequest);

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
        String mainUrl = "https://tixclick.site/payment/queue";
        String fallbackUrl = "https://tixclick.site/payment/queue"; // hoặc URL khác nếu cần

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
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getAllPayments(){
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
    public ResponseEntity<ApiResponse<String>> testQR(@RequestBody TicketQrCodeDTO ticketQrCodeDTO){
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
}
