package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.TicketDTO;
import com.pse.tixclick.payload.request.TicketRequest;
import com.pse.tixclick.payload.request.create.CreateTickeSeatMaptRequest;
import com.pse.tixclick.payload.request.create.CreateTicketRequest;
import com.pse.tixclick.payload.request.update.UpdateTicketRequest;

import java.util.List;

public interface TicketService {
    TicketDTO createTicket(CreateTicketRequest ticketDTO);

    TicketDTO updateTicket(UpdateTicketRequest ticketDTO, int ticketId);

    List<TicketRequest> getAllTicketByEventId(int eventId);

    List<TicketRequest> deleteTicket(String ticketCode);

    List<TicketRequest> createTicketSeatMap(CreateTickeSeatMaptRequest request);

    List<TicketRequest> updateTicketSeatMap(CreateTickeSeatMaptRequest request);
}
