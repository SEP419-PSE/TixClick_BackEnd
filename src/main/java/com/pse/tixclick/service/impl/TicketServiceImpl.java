package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.TicketDTO;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.request.create.CreateTicketRequest;
import com.pse.tixclick.payload.request.update.UpdateTicketRequest;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.EventActivityRepository;
import com.pse.tixclick.repository.TicketRepository;
import com.pse.tixclick.service.TicketService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TicketServiceImpl implements TicketService {
    AccountRepository accountRepository;
    EventActivityRepository eventActivityRepository;
    TicketRepository ticketRepository;
    ModelMapper modelMapper;

    @Override
    public TicketDTO createTicket(CreateTicketRequest ticketDTO) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        var account = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        var eventActivity = eventActivityRepository.findById(ticketDTO.getEventActivityId())
                .orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_NOT_FOUND));

        var ticket = new Ticket();
        ticket.setTicketName(ticketDTO.getTicketName());
        ticket.setQuantity(ticketDTO.getQuantity());
        ticket.setCreatedDate(ticketDTO.getCreatedDate());
        ticket.setPrice(ticketDTO.getPrice());
        ticket.setStartDate(ticketDTO.getStartDate());
        ticket.setEndDate(ticketDTO.getEndDate());
        ticket.setMinQuantity(ticketDTO.getMinQuantity());
        ticket.setMaxQuantity(ticketDTO.getMaxQuantity());
        ticket.setStatus(ticketDTO.isStatus());
        ticket.setAccount(account);
        ticket.setEventActivity(eventActivity);

        ticketRepository.save(ticket);
        return modelMapper.map(ticket, TicketDTO.class);

    }

    @Override
    public TicketDTO updateTicket(UpdateTicketRequest ticketDTO, int ticketId) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        var account = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        var ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));


    return null;
    }

}
