package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.ContractDetailDTO;
import com.pse.tixclick.payload.dto.MonthlySalesReportDTO;
import com.pse.tixclick.payload.dto.TransactionCompanyByEventDTO;
import com.pse.tixclick.payload.dto.TransactionDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.payment.ContractPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.pse.tixclick.payload.entity.payment.Payment;
import com.pse.tixclick.payload.entity.payment.Transaction;
import com.pse.tixclick.payload.response.PaginationResponse;
import com.pse.tixclick.repository.CompanyRepository;
import com.pse.tixclick.repository.EventRepository;
import com.pse.tixclick.repository.OrderDetailRepository;
import com.pse.tixclick.repository.TransactionRepository;
import com.pse.tixclick.service.TransactionService;
import com.pse.tixclick.utils.AppUtils;
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
    OrderDetailRepository orderDetailRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    AppUtils appUtils;

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
                        transaction.getType().name(),
                        transaction.getTransactionDate(),
                        Optional.ofNullable(transaction.getAccount()).map(Account::getAccountId).orElse(0),
                        Optional.ofNullable(transaction.getPayment()).map(Payment::getPaymentId).orElse(0),
                        Optional.ofNullable(transaction.getContractPayment()).map(ContractPayment::getContractPaymentId).orElse(0)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Double totalTransaction() {
        Double sum = transactionRepository.sumTotalTransaction();
        if(sum == null) {
            return 0.0;
        }
        return sum;
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


    @Override
    public PaginationResponse<TransactionCompanyByEventDTO> getTransactionCompanyByEvent(
            int eventId, int page, int size, String sortDirection) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Company company = event.getCompany();
        if (appUtils.getAccountFromAuthentication().getAccountId() != company.getRepresentativeId().getAccountId()) {
            throw new AppException(ErrorCode.EVENT_NOT_COMPANY);
        }

        // Lấy tất cả các giao dịch từ repository (không phân trang)
        List<Transaction> transactions = transactionRepository.findAllByEventId(eventId);

        // Sắp xếp danh sách giao dịch theo transactionDate
        transactions.sort((t1, t2) -> {
            if (sortDirection.equalsIgnoreCase("desc")) {
                return t2.getTransactionDate().compareTo(t1.getTransactionDate());
            } else {
                return t1.getTransactionDate().compareTo(t2.getTransactionDate());
            }
        });

        // Tính toán phân trang
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, transactions.size());

        // Kiểm tra nếu có dữ liệu
        if (transactions.isEmpty()) {
            throw new AppException(ErrorCode.TRANSACTION_NOT_FOUND);
        }

        // Lấy danh sách giao dịch theo trang
        List<Transaction> pagedTransactions = transactions.subList(startIndex, endIndex);

        // Chuyển đổi các Transaction thành DTO
        List<TransactionCompanyByEventDTO> dtos = pagedTransactions.stream().map(transaction -> {
            var payment = transaction.getPayment();
            if (payment == null) throw new AppException(ErrorCode.PAYMENT_NOT_FOUND);

            var order = payment.getOrder();
            String note = (order.getNote() == null) ? "Thanh toán vé" : order.getNote();

            return TransactionCompanyByEventDTO.builder()
                    .transactionCode(transaction.getTransactionCode())
                    .transactionDate(transaction.getTransactionDate())
                    .amount(transaction.getAmount())
                    .status(transaction.getStatus().name())
                    .note(note)
                    .accountName(transaction.getAccount().getUserName())
                    .accountMail(transaction.getAccount().getEmail())
                    .transactionType(transaction.getType().name())
                    .build();
        }).toList();

        // Tính tổng số trang
        int totalElements = transactions.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PaginationResponse.<TransactionCompanyByEventDTO>builder()
                .items(dtos)
                .currentPage(page)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .pageSize(size)
                .build();
    }


}
