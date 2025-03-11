package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.MonthlySalesReportDTO;

import java.util.List;

public interface TransactionService {
    double sumTotalTransaction();
    List<MonthlySalesReportDTO> getMonthlySalesReport();
}
