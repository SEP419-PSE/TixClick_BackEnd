package com.pse.tixclick.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketsSoldAndRevenueDTO {
    private int days;
    private int totalTicketsSold;
    private double totalRevenue;
    private int totalEvents;
    private double avgDailyRevenue;
    private double revenueGrowth;
}
