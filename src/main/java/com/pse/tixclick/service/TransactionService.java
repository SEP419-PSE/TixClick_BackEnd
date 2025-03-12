package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.MonthlySalesReportDTO;
import com.pse.tixclick.payload.dto.TransactionDTO;

import java.util.List;

public interface TransactionService {
    double sumTotalTransaction();

    List<MonthlySalesReportDTO> getMonthlySalesReport();

    List<TransactionDTO> getTransactions();
}
