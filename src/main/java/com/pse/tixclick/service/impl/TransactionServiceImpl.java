package com.pse.tixclick.service.impl;

import com.pse.tixclick.repository.TransactionRepository;
import com.pse.tixclick.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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


}
