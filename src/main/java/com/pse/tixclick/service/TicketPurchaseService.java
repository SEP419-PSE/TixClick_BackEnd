package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.*;
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

    List<MyTicketDTO> getTicketPurchasesByAccount();

    TicketQrCodeDTO decryptQrCode(String qrCode) throws Exception;
}
