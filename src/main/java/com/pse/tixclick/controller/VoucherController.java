package com.pse.tixclick.controller;

import com.pse.tixclick.payload.dto.VoucherDTO;
import com.pse.tixclick.payload.request.create.CreateVoucherRequest;
import com.pse.tixclick.payload.response.ApiResponse;
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

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> getAllVouchers() {
        try {
            List<VoucherDTO> voucherDTOList = voucherService.getAllVouchers();
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
}
