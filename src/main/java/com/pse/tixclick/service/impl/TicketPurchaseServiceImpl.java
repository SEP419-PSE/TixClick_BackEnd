package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.dto.TicketPurchaseDTO;
import com.pse.tixclick.payload.dto.TicketQrCodeDTO;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.seatmap.Seat;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.TicketPurchaseService;
import com.pse.tixclick.utils.AppUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class TicketPurchaseServiceImpl implements TicketPurchaseService {
    private static final int QR_CODE_WIDTH = 400;
    private static final int QR_CODE_HEIGHT = 400;

    @Autowired
    AppUtils appUtils;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    TicketPurchaseRepository ticketPurchaseRepository;

    @Autowired
    ZoneRepository zoneRepository;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    EventActivityRepository eventActivityRepository;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    EventRepository eventRepository;


    @Override
    public TicketPurchaseDTO createTicketPurchase(CreateTicketPurchaseRequest createTicketPurchaseRequest) {
        TicketPurchase ticketPurchase = new TicketPurchase();
        ticketPurchase.setStatus("PENDING");

        Zone zone = zoneRepository
                .findById(createTicketPurchaseRequest.getZoneId())
                .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

        Seat seat = seatRepository
                .findById(createTicketPurchaseRequest.getSeatId())
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));

        EventActivity eventActivity = eventActivityRepository
                .findById(createTicketPurchaseRequest.getEventActivityId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

        Ticket ticket = ticketRepository
                .findById(createTicketPurchaseRequest.getTicketId())
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

        Event event = eventRepository
                .findById(createTicketPurchaseRequest.getEventId())
                .orElseThrow(()-> new AppException(ErrorCode.EVENT_NOT_FOUND));

        ticketPurchase.setZone(zone);
        ticketPurchase.setSeat(seat);
        ticketPurchase.setEventActivity(eventActivity);
        ticketPurchase.setTicket(ticket);
        ticketPurchase.setEvent(event);

        TicketQrCodeDTO ticketQrCodeDTO = new TicketQrCodeDTO();
        ticketQrCodeDTO.setTicket_name(ticket.getTicketName());
        ticketQrCodeDTO.setPurchase_date(new Date());
        ticketQrCodeDTO.setEvent_name(event.getEventName());
        ticketQrCodeDTO.setActivity_name(eventActivity.getActivityName());
        ticketQrCodeDTO.setZone_name(zone.getZoneName());
        ticketQrCodeDTO.setSeat_row_number(seat.getRowNumber());
        ticketQrCodeDTO.setSeat_column_number(seat.getColumnNumber());
        ticketQrCodeDTO.setAccount_name(appUtils.getAccountFromAuthentication().getUserName());
        ticketQrCodeDTO.setPhone(appUtils.getAccountFromAuthentication().getPhone());

        String qrCode = generateQRCode(ticketQrCodeDTO);
        ticketPurchase.setQrCode(qrCode);

        ticketPurchaseRepository.save(ticketPurchase);

        return null;
    }

    private String generateQRCode(TicketQrCodeDTO ticketQrCodeDTO){
        String dataTransfer = AppUtils.transferToString(ticketQrCodeDTO);
        return AppUtils.generateQRCode(dataTransfer, QR_CODE_WIDTH, QR_CODE_HEIGHT);
    }
}
