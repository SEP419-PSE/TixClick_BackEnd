package com.pse.tixclick.service.impl;

import com.pse.tixclick.payload.dto.VoucherDTO;
import com.pse.tixclick.payload.entity.payment.Voucher;
import com.pse.tixclick.payload.request.create.CreateVoucherRequest;
import com.pse.tixclick.repository.VoucherRepository;
import com.pse.tixclick.service.VoucherService;
import com.pse.tixclick.utils.AppUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class VoucherServiceImpl implements VoucherService {
    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    AppUtils appUtils;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public VoucherDTO createVoucher(CreateVoucherRequest createVoucherRequest) {
        if (createVoucherRequest.getVoucherName() == null || createVoucherRequest.getVoucherCode() == null) {
            throw new IllegalArgumentException("Voucher name and code cannot be null");
        }
        if (createVoucherRequest.getDiscount() <= 0) {
            throw new IllegalArgumentException("Discount must be greater than 0");
        }
        if (voucherRepository.existsByVoucherCode(createVoucherRequest.getVoucherCode())) {
            throw new IllegalArgumentException("Voucher code already exists");
        }

        Voucher voucher = new Voucher();
        voucher.setVoucherName(createVoucherRequest.getVoucherName());
        voucher.setVoucherCode(createVoucherRequest.getVoucherCode());
        voucher.setDiscount(createVoucherRequest.getDiscount());
        voucher.setStatus("ACTIVE");
        voucher.setCreatedDate(LocalDateTime.now());
        voucher.setAccount(appUtils.getAccountFromAuthentication());

        voucherRepository.save(voucher);

        return modelMapper.map(voucher, VoucherDTO.class);
    }
}
