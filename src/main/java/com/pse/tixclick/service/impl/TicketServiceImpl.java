package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.TicketDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.request.TicketRequest;
import com.pse.tixclick.payload.request.create.CreateTickeSeatMaptRequest;
import com.pse.tixclick.payload.request.create.CreateTicketRequest;
import com.pse.tixclick.payload.request.update.UpdateTicketRequest;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.EventActivityRepository;
import com.pse.tixclick.repository.EventRepository;
import com.pse.tixclick.repository.TicketRepository;
import com.pse.tixclick.service.TicketService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class TicketServiceImpl implements TicketService {
    AccountRepository accountRepository;
    EventActivityRepository eventActivityRepository;
    TicketRepository ticketRepository;
    ModelMapper modelMapper;
    EventRepository eventRepository;
    @Override
    public TicketDTO createTicket(CreateTicketRequest ticketDTO) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        var account = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        var event = eventRepository.findById(ticketDTO.getEventId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        var ticket = new Ticket();
        ticket.setTicketName(ticketDTO.getTicketName());
        ticket.setTicketCode(ticketDTO.getTicketCode());    
        ticket.setCreatedDate(LocalDateTime.now());
        ticket.setPrice(ticketDTO.getPrice());
        ticket.setMinQuantity(ticketDTO.getMinQuantity());
        ticket.setMaxQuantity(ticketDTO.getMaxQuantity());
        ticket.setStatus(ticketDTO.isStatus());
        ticket.setAccount(account);
        ticket.setTextColor(ticketDTO.getTextColor());
        ticket.setSeatBackgroundColor(ticketDTO.getSeatBackgroundColor());
        ticket.setEvent(event);
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

    @Override
    public List<TicketRequest> getAllTicketByEventId(int eventId) {
        List<Ticket> tickets = ticketRepository.findTicketsByEvent_EventId(eventId);
        return tickets.stream().map(ticket -> TicketRequest.builder()
                .id(ticket.getTicketCode())
                .name(ticket.getTicketName())
                .color(ticket.getSeatBackgroundColor())
                .textColor(ticket.getTextColor())
                .price(ticket.getPrice())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<TicketRequest> deleteTicket(String ticketCode) {
        var ticket = ticketRepository.findTicketByTicketCode(ticketCode)
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));
        ticketRepository.delete(ticket);
        return getAllTicketByEventId(ticket.getEvent().getEventId());
    }

    @Override
    public List<TicketRequest> createTicketSeatMap(CreateTickeSeatMaptRequest request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        var account = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Kiểm tra ticketCode đã tồn tại chưa
        if (ticketRepository.findTicketByTicketCode(request.getId()).isPresent()) {
            throw new AppException(ErrorCode.TICKET_EXISTED);
        }

        var event = eventRepository.findEventByEventId(request.getEventId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // Tạo vé mới
        Ticket newTicket = new Ticket();
        newTicket.setMinQuantity(request.getMinQuantity());
        newTicket.setMaxQuantity(request.getMaxQuantity());
        newTicket.setPrice(request.getPrice());
        newTicket.setTicketName(request.getName());
        newTicket.setSeatBackgroundColor(request.getColor());
        newTicket.setTextColor(request.getTextColor());
        newTicket.setEvent(event);
        newTicket.setAccount(account);
        newTicket.setTicketCode(request.getId());
        newTicket.setCreatedDate(LocalDateTime.now()); // Thời gian tạo vé

        // Lưu vào database
        ticketRepository.save(newTicket);

        // Chuyển đổi thành DTO để trả về
        return getAllTicketByEventId(event.getEventId());
    }



}
