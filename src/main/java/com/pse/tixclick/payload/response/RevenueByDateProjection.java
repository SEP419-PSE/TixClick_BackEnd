package com.pse.tixclick.payload.response;

import java.time.LocalDate;

public interface RevenueByDateProjection {
    LocalDate getOrderDay();
    double getTotalRevenue();
}
