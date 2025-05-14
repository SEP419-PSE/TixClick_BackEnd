package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.*;
import com.pse.tixclick.payload.request.create.CheckinRequest;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;
import com.pse.tixclick.payload.request.create.ListTicketPurchaseRequest;
import com.pse.tixclick.payload.response.MyTicketResponse;
import com.pse.tixclick.payload.response.PaginationResponse;
import com.pse.tixclick.payload.response.TicketQRResponse;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface TicketPurchaseService {
    List<TicketPurchaseDTO> createTicketPurchase(ListTicketPurchaseRequest createTicketPurchaseRequest) throws Exception;

    String checkinTicketPurchase(int checkinId);

    int countTotalTicketSold();

    List<TicketSalesResponse> getMonthlyTicketSales();

    int countTotalCheckins();

    TicketsSoldAndRevenueDTO getTicketsSoldAndRevenueByDay(int day);

    PaginationResponse<MyTicketResponse> getTicketPurchasesByAccount(int page, int size, String sortDirection);

    PaginationResponse<MyTicketDTO> searchTicketPurchasesByEventName(int page, int size, String sortDirection, String eventName);
    TicketQRResponse decryptQrCode(String qrCode);

    int countTicketPurchaseStatusByPurchased();

    int printActiveThreads();

    String cancelTicketPurchase(List<Integer> ticketPurchaseIds);

    MyTicketDTO getTicketPurchaseById(int ticketPurchaseId);

}
