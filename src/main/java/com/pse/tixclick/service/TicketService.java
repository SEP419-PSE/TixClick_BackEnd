package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.TicketDTO;
import com.pse.tixclick.payload.request.CreateTicketRequest;

public interface TicketService {
    TicketDTO createTicket(CreateTicketRequest ticketDTO);


}
