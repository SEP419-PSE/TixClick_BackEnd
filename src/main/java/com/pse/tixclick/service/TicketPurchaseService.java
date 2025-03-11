package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.TicketPurchaseDTO;
import com.pse.tixclick.payload.request.create.CheckinRequest;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;

public interface TicketPurchaseService {
    TicketPurchaseDTO createTicketPurchase(CreateTicketPurchaseRequest createTicketPurchaseRequest);

    String checkinTicketPurchase(int checkinId);

    int countTotalTicketSold();
}
