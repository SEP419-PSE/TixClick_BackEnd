package com.pse.tixclick.controller;

import com.pse.tixclick.payload.dto.OrderDTO;
import com.pse.tixclick.payload.dto.Order_OrderDetailDTO;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    @Autowired
    OrderService orderService;

    @GetMapping("/all_of_user")
    public ResponseEntity<ApiResponse<List<Order_OrderDetailDTO>>> getAllOrdersOfUser() {
        try {
            List<Order_OrderDetailDTO> orderDTOList = orderService.getAllOrderOfUser();
            return ResponseEntity.ok(
                    ApiResponse.<List<Order_OrderDetailDTO>>builder()
                            .code(200)
                            .message("Success")
                            .result(orderDTOList)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<List<Order_OrderDetailDTO>>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/all_of_user/{status}")
    public ResponseEntity<ApiResponse<List<Order_OrderDetailDTO>>> getOrderStatusOfUser(@PathVariable String status) {
        try {
            List<Order_OrderDetailDTO> orderDTOList = orderService.getOrderStatusOfUser(status);
            return ResponseEntity.ok(
                    ApiResponse.<List<Order_OrderDetailDTO>>builder()
                            .code(200)
                            .message("Success")
                            .result(orderDTOList)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<List<Order_OrderDetailDTO>>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }
}
