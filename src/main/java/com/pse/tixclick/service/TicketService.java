package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.TicketDTO;
import com.pse.tixclick.payload.request.create.CreateTicketRequest;
import com.pse.tixclick.payload.request.update.UpdateTicketRequest;

public interface TicketService {
    TicketDTO createTicket(CreateTicketRequest ticketDTO);

    TicketDTO updateTicket(UpdateTicketRequest ticketDTO, int ticketId);
}
