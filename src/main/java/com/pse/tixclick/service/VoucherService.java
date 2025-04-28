package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.VoucherDTO;
import com.pse.tixclick.payload.entity.entity_enum.EVoucherStatus;
import com.pse.tixclick.payload.request.create.CreateVoucherRequest;

import java.util.List;

public interface VoucherService {
    VoucherDTO createVoucher(CreateVoucherRequest createVoucherRequest);

    List<VoucherDTO> getAllVouchers(int eventId, EVoucherStatus status);

    String changeVoucherStatus(int voucherId,EVoucherStatus status);

    String checkVoucherCode(String voucherCode, int eventId);
}
