package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.VoucherDTO;
import com.pse.tixclick.payload.entity.entity_enum.EVoucherStatus;
import com.pse.tixclick.payload.request.create.CreateVoucherRequest;
import com.pse.tixclick.payload.response.VoucherPercentageResponse;

import java.util.List;

public interface VoucherService {
    VoucherDTO createVoucher(CreateVoucherRequest createVoucherRequest);

    List<VoucherDTO> getAllVouchers(int eventId, EVoucherStatus status);

    String changeVoucherStatus(int voucherId);

    VoucherPercentageResponse checkVoucherCode(String voucherCode, int eventId);
}
