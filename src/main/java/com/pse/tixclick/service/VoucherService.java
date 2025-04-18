package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.VoucherDTO;
import com.pse.tixclick.payload.request.create.CreateVoucherRequest;

import java.util.List;

public interface VoucherService {
    VoucherDTO createVoucher(CreateVoucherRequest createVoucherRequest);

    List<VoucherDTO> getAllVouchers(int eventId, String status);

    String changeVoucherStatus(int voucherId);

    String checkVoucherCode(String voucherCode, int eventId);
}
