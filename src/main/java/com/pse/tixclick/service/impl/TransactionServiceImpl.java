package com.pse.tixclick.service.impl;

import com.pse.tixclick.payload.dto.MonthlySalesReportDTO;
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
}
