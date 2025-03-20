package com.pse.tixclick.service.impl;

import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.entity.company.ContractDetail;
import com.pse.tixclick.payload.entity.entity_enum.EContractDetailStatus;
import com.pse.tixclick.payload.entity.entity_enum.ETransactionType;
import com.pse.tixclick.payload.entity.payment.ContractPayment;
import com.pse.tixclick.payload.entity.payment.Transaction;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.ContractPaymentService;
import com.pse.tixclick.utils.AppUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContractPaymentServiceImpl implements ContractPaymentService {
    @Autowired
    ContractDetailRepository contractDetailRepository;

    @Autowired
    ContractRepository contractRepository;

    @Autowired
    ContractPaymentRepository contractPaymentRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    AppUtils appUtils;

    @Autowired
    ModelMapper modelMapper;



    @Override
    public String payContractPayment(int contractPaymentId) {
        ContractPayment contractPayment = contractPaymentRepository
                .findById(contractPaymentId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_PAYMENT_NOT_FOUND));

        contractPayment.setStatus(EContractDetailStatus.PAID.name());
        contractPayment.setPaymentDate(LocalDateTime.now());
        contractPaymentRepository.save(contractPayment);

        ContractDetail contractDetail = contractDetailRepository
                .findByContractPaymentId(contractPaymentId);
        contractDetail.setStatus(EContractDetailStatus.PAID.name());
        contractDetailRepository.save(contractDetail);

        Transaction transaction = new Transaction();
        transaction.setAmount(contractPayment.getPaymentAmount());
        transaction.setDescription("Payment for " + contractDetail.getContractDetailName());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setContractPayment(contractPayment);
        transaction.setAccount(appUtils.getAccountFromAuthentication());
        transaction.setType(ETransactionType.CONTRACT_PAYMENT.name());
        transactionRepository.save(transaction);

        return "Payment for Contract successful!";
    }
}
