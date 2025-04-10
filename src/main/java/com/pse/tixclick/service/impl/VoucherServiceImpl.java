package com.pse.tixclick.service.impl;

import com.pse.tixclick.payload.dto.VoucherDTO;
import com.pse.tixclick.payload.entity.entity_enum.EVoucherStatus;
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
import java.util.List;

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
        voucher.setStatus(EVoucherStatus.ACTIVE);
        voucher.setCreatedDate(LocalDateTime.now());
        voucher.setAccount(appUtils.getAccountFromAuthentication());

        voucherRepository.save(voucher);

        return modelMapper.map(voucher, VoucherDTO.class);
    }

    @Override
    public List<VoucherDTO> getAllVouchers() {
        List<Voucher> vouchers = voucherRepository.findAll();
        return vouchers.stream()
                .map(voucher -> modelMapper.map(voucher, VoucherDTO.class))
                .toList();
    }

    @Override
    public String changeVoucherStatus(int voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found"));

        if (voucher.getStatus().equals(EVoucherStatus.ACTIVE)) {
            voucher.setStatus(EVoucherStatus.INACTIVE);
        } else {
            return "Voucher is already inactive";
        }
        voucherRepository.save(voucher);
        return "Voucher status changed to inactive";
    }
}
