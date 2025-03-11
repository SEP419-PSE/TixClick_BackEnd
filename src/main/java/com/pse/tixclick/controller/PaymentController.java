package com.pse.tixclick.controller;

import com.pse.tixclick.payload.dto.OrderDTO;
import com.pse.tixclick.payload.dto.PaymentDTO;
import com.pse.tixclick.payload.dto.SeatDTO;
import com.pse.tixclick.payload.request.create.CreateOrderRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.PayOSResponse;
import com.pse.tixclick.payload.response.PaymentResponse;
import com.pse.tixclick.payload.response.ResponseObject;
import com.pse.tixclick.payos.PayOSUtils;
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
    public ResponseObject<PayOSResponse>payOS(@RequestBody CreateOrderRequest request, HttpServletRequest httpServletRequest) throws Exception {
        OrderDTO orderDTO = orderService.createOrder(request);
        return new ResponseObject<>(HttpStatus.OK, "Success", paymentService.createPaymentLink(orderDTO.getOrderId(), httpServletRequest));
    }

    @GetMapping("/payos_call_back")
    public void payOSCallbackHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = "https://tixclick.site/payment-return";
        String urlFail = "https://tixclick.site/payment-return";

        PaymentResponse payment = paymentService.handleCallbackPayOS(request);
        if (payment.getCode().equals("00")) {
            response.sendRedirect(url);
        } else {
            response.sendRedirect(urlFail);
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
}
