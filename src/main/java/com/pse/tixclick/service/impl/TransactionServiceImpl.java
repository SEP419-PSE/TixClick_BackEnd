package com.pse.tixclick.service.impl;

import com.pse.tixclick.payload.dto.MonthlySalesReportDTO;
import com.pse.tixclick.payload.dto.TransactionDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.company.Contract;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.payment.ContractPayment;
import com.pse.tixclick.payload.entity.payment.Payment;
import com.pse.tixclick.payload.entity.payment.Transaction;
import com.pse.tixclick.repository.ContractRepository;
import com.pse.tixclick.repository.OrderRepository;
import com.pse.tixclick.repository.TransactionRepository;
import com.pse.tixclick.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    ContractRepository contractRepository;


    @Override
    public double sumTotalTransaction() {
        Double sum = transactionRepository.sumTotalTransaction();
        return sum == null ? 0 : sum;
    }

    @Override
    public List<MonthlySalesReportDTO> getMonthlySalesReport() {
        List<Object[]> results = transactionRepository.getMonthlySalesReport();
        return results.stream()
                .map(row -> new MonthlySalesReportDTO(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).doubleValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getTransactions() {
        List<Transaction> results = transactionRepository.findAll();
        return results.stream()
                .map(transaction -> new TransactionDTO(
                        transaction.getTransactionId(),
                        transaction.getAmount(),
                        transaction.getDescription(),
                        transaction.getTransactionCode(),
                        transaction.getType(),
                        transaction.getTransactionDate(),
                        Optional.ofNullable(transaction.getAccount()).map(Account::getAccountId).orElse(0),
                        Optional.ofNullable(transaction.getPayment()).map(Payment::getPaymentId).orElse(0),
                        Optional.ofNullable(transaction.getContractPayment()).map(ContractPayment::getContractPaymentId).orElse(0)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public double totalTransaction() {
        return transactionRepository.sumTotalTransaction();
    }

    @Override
    public double totalCommission() {
//        List<Transaction> transactions = transactionRepository.findAll();
//        double totalCommission = 0;
//
//        for (Transaction transaction : transactions) {
//            Event event = transaction.getPayment().getContractPayment().getContract().getEvent();
//            Contract contract = event.getContract();
//
//            if (contract != null) {
//                double commissionRate = Double.parseDouble(contract.getCommission()) / 100; // Convert hoa hồng từ String sang double
//                totalCommission += transaction.getAmount() * commissionRate;
//            }
//        }
//        return totalCommission;
        return  0;
    }
}
