package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.TicketPurchaseDTO;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;

public interface TicketPurchaseService {
    TicketPurchaseDTO createTicketPurchase(CreateTicketPurchaseRequest createTicketPurchaseRequest);
}
