package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.MonthlySalesReportDTO;
import com.pse.tixclick.payload.dto.TransactionCompanyByEventDTO;
import com.pse.tixclick.payload.dto.TransactionDTO;
import com.pse.tixclick.payload.response.PaginationResponse;

import java.util.List;

public interface TransactionService {
    double sumTotalTransaction();

    List<MonthlySalesReportDTO> getMonthlySalesReport();

    List<TransactionDTO> getTransactions();

    Double totalTransaction();

    double totalCommission();

    PaginationResponse<TransactionCompanyByEventDTO> getTransactionCompanyByEvent(
            int eventId, int page, int size, String sortDirection);
}
