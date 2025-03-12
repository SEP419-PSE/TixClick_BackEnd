package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.TicketPurchaseDTO;
import com.pse.tixclick.payload.dto.TicketSalesResponse;
import com.pse.tixclick.payload.dto.TicketsSoldAndRevenueDTO;
import com.pse.tixclick.payload.request.create.CheckinRequest;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;

import java.util.List;

public interface TicketPurchaseService {
    TicketPurchaseDTO createTicketPurchase(CreateTicketPurchaseRequest createTicketPurchaseRequest);

    String checkinTicketPurchase(int checkinId);

    int countTotalTicketSold();

    List<TicketSalesResponse> getMonthlyTicketSales();

    int countTotalCheckins();

    TicketsSoldAndRevenueDTO getTicketsSoldAndRevenueByDay(int day);

}
