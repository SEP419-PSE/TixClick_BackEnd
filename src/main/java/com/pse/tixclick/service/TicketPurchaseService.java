package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.*;
import com.pse.tixclick.payload.request.create.CheckinRequest;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;
import com.pse.tixclick.payload.request.create.ListTicketPurchaseRequest;

import java.util.List;

public interface TicketPurchaseService {
    List<TicketPurchaseDTO> createTicketPurchase(ListTicketPurchaseRequest createTicketPurchaseRequest);

    String checkinTicketPurchase(int checkinId);

    int countTotalTicketSold();

    List<TicketSalesResponse> getMonthlyTicketSales();

    int countTotalCheckins();

    TicketsSoldAndRevenueDTO getTicketsSoldAndRevenueByDay(int day);

    List<MyTicketDTO> getTicketPurchasesByAccount(int page, int size);
    TicketQrCodeDTO decryptQrCode(String qrCode);

    int countTicketPurchaseStatusByPurchased();

    int printActiveThreads();

    String cancelTicketPurchase(List<Integer> ticketPurchaseIds);

    MyTicketDTO getTicketPurchaseById(int ticketPurchaseId);
}
