package com.pse.tixclick.controller;

import com.pse.tixclick.payload.dto.VoucherDTO;
import com.pse.tixclick.payload.entity.entity_enum.EVoucherStatus;
import com.pse.tixclick.payload.request.create.CreateVoucherRequest;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.payload.response.VoucherPercentageResponse;
import com.pse.tixclick.service.VoucherService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/voucher")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VoucherController {
    @Autowired
    VoucherService voucherService;


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<VoucherDTO>> createVoucher(@RequestBody CreateVoucherRequest createVoucherRequest) {
        try {
            VoucherDTO voucherDTO = voucherService.createVoucher(createVoucherRequest);
            return ResponseEntity.ok(
                    ApiResponse.<VoucherDTO>builder()
                            .code(200)
                            .message("Voucher created successfully")
                            .result(voucherDTO)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<VoucherDTO>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @GetMapping("/all/{eventId}/{status}")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> getAllVouchers(@PathVariable int eventId, @PathVariable EVoucherStatus status) {
        try {
            List<VoucherDTO> voucherDTOList = voucherService.getAllVouchers(eventId, status);
            return ResponseEntity.ok(
                    ApiResponse.<List<VoucherDTO>>builder()
                            .code(200)
                            .message("Vouchers retrieved successfully")
                            .result(voucherDTOList)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<List<VoucherDTO>>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }

    @DeleteMapping("/delete/{voucherId}")
    public ResponseEntity<ApiResponse<String>> changeVoucherStatus(@PathVariable int voucherId) {
        try {
            String message = voucherService.changeVoucherStatus(voucherId);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .code(200)
                            .message(message)
                            .result(null)
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

    @GetMapping("/check/{voucherCode}/{eventId}")
    public ResponseEntity<ApiResponse<VoucherPercentageResponse>> checkVoucherCode(@PathVariable String voucherCode, @PathVariable int eventId) {
        try {
            VoucherPercentageResponse message = voucherService.checkVoucherCode(voucherCode, eventId);
            return ResponseEntity.ok(
                    ApiResponse.<VoucherPercentageResponse>builder()
                            .code(200)
                            .result(message)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.<VoucherPercentageResponse>builder()
                            .code(400)
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }
}
