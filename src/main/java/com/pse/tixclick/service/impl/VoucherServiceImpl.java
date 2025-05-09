package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.VoucherDTO;
import com.pse.tixclick.payload.entity.entity_enum.ERole;
import com.pse.tixclick.payload.entity.entity_enum.EVoucherStatus;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.payment.Voucher;
import com.pse.tixclick.payload.request.create.CreateVoucherRequest;
import com.pse.tixclick.payload.response.VoucherPercentageResponse;
import com.pse.tixclick.repository.CompanyRepository;
import com.pse.tixclick.repository.EventRepository;
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

    @Autowired
    EventRepository eventRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Override
    public VoucherDTO createVoucher(CreateVoucherRequest createVoucherRequest) {
        if (!appUtils.getAccountFromAuthentication().getRole().getRoleName().equals(ERole.ORGANIZER)) {
            throw new AppException(ErrorCode.NOT_PERMISSION);
        }

        if (createVoucherRequest.getVoucherName() == null || createVoucherRequest.getVoucherCode() == null) {
            throw new IllegalArgumentException("Voucher name and code cannot be null");
        }
        if (createVoucherRequest.getDiscount() <= 0) {
            throw new IllegalArgumentException("Discount must be greater than 0");
        }

        Voucher existingVoucher = voucherRepository.existsByVoucherCode(createVoucherRequest.getVoucherCode());
        if (existingVoucher != null) {
            throw new AppException(ErrorCode.VOUCHER_CODE_ALREADY_EXISTS);
        }

        Event event = eventRepository.findById(createVoucherRequest.getEventId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        if (event.getCompany().getRepresentativeId().getAccountId() != appUtils.getAccountFromAuthentication().getAccountId()) {
            throw new AppException(ErrorCode.NOT_PERMISSION);
        }

        Voucher voucher = new Voucher();
        voucher.setVoucherName(createVoucherRequest.getVoucherName());
        voucher.setVoucherCode(createVoucherRequest.getVoucherCode());
        voucher.setDiscount(createVoucherRequest.getDiscount());
        voucher.setStatus(EVoucherStatus.ACTIVE);
        voucher.setCreatedDate(LocalDateTime.now());
        voucher.setQuantity(createVoucherRequest.getQuantity());
        voucher.setStartDate(createVoucherRequest.getStartDate());
        voucher.setEndDate(createVoucherRequest.getEndDate());
        voucher.setAccount(appUtils.getAccountFromAuthentication());
        voucher.setEvent(event);

        voucherRepository.save(voucher);

        return modelMapper.map(voucher, VoucherDTO.class);
    }

    @Override
    public List<VoucherDTO> getAllVouchers(int eventId, EVoucherStatus status) {
        List<Voucher> vouchers = voucherRepository.findVouchersByStatusAndEvent_EventId(status, eventId);
        return vouchers.stream()
                .map(voucher -> modelMapper.map(voucher, VoucherDTO.class))
                .toList();
    }

    @Override
    public String changeVoucherStatus(int voucherId, EVoucherStatus status) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        if (voucher.getEvent() == null) {
            throw new AppException(ErrorCode.VOUCHER_NOT_FOUND);
        }

        voucher.setStatus(status);
        voucherRepository.save(voucher);
        return "Voucher status changed successfully";
    }

    @Override
    public VoucherPercentageResponse checkVoucherCode(String voucherCode, int eventId) {
        Voucher voucher = voucherRepository.findByVoucherCodeAndEvent(voucherCode, eventId)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
        VoucherPercentageResponse voucherPercentageResponse = new VoucherPercentageResponse();
        if (voucher.getEvent() != null && voucher.getEvent().getEventId() != eventId) {
            throw new AppException(ErrorCode.VOUCHER_INACTIVE);
        }

        if (voucher.getStatus().equals(EVoucherStatus.ACTIVE)) {
            voucherPercentageResponse.setDiscount(voucher.getDiscount());
            voucherPercentageResponse.setVoucherCode(voucherCode);
            voucherPercentageResponse.setVoucherName(voucher.getVoucherName());
            voucherPercentageResponse.setNotice("Voucher is valid for this event");
            return voucherPercentageResponse;
        } else {
            throw new AppException(ErrorCode.VOUCHER_INACTIVE);
        }
    }
}
