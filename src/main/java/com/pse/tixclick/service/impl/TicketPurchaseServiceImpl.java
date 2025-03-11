package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.dto.TicketDTO;
import com.pse.tixclick.payload.dto.TicketPurchaseDTO;
import com.pse.tixclick.payload.dto.TicketQrCodeDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.CheckinLog;
import com.pse.tixclick.payload.entity.entity_enum.ECheckinLogStatus;
import com.pse.tixclick.payload.entity.entity_enum.ETicketPurchaseStatus;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.seatmap.Seat;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import com.pse.tixclick.payload.request.create.CheckinRequest;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class TicketPurchaseServiceImpl implements TicketPurchaseService {
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

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    CheckinLogRepository checkinLogRepository;


    @Override
    public TicketPurchaseDTO createTicketPurchase(CreateTicketPurchaseRequest createTicketPurchaseRequest) {
        TicketPurchase ticketPurchase = new TicketPurchase();
        ticketPurchase.setStatus(ETicketPurchaseStatus.PENDING.name());

        Zone zone = zoneRepository.findById(createTicketPurchaseRequest.getZoneId())
                .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

        if (zone.getAvailableQuantity() <= 0) {
            throw new AppException(ErrorCode.ZONE_FULL);
        }
        zone.setAvailableQuantity(zone.getAvailableQuantity() - 1);
        if (zone.getAvailableQuantity() == 0) {
            zone.setStatus(false);
        }
        zoneRepository.save(zone);

        Seat seat = seatRepository.findById(createTicketPurchaseRequest.getSeatId())
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));

        if (!seat.isStatus()) {
            throw new AppException(ErrorCode.SEAT_NOT_AVAILABLE);
        }
        seat.setStatus(false);
        seatRepository.save(seat);

        EventActivity eventActivity = eventActivityRepository.findById(createTicketPurchaseRequest.getEventActivityId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

        Ticket ticket = ticketRepository.findById(createTicketPurchaseRequest.getTicketId())
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

        Event event = eventRepository.findById(createTicketPurchaseRequest.getEventId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        ticketPurchase.setZone(zone);
        ticketPurchase.setSeat(seat);
        ticketPurchase.setEventActivity(eventActivity);
        ticketPurchase.setTicket(ticket);
        ticketPurchase.setEvent(event);
        ticketPurchase.setQrCode(null);

        ticketPurchase = ticketPurchaseRepository.save(ticketPurchase);

        TicketPurchaseDTO ticketPurchaseDTO = new TicketPurchaseDTO();
        ticketPurchaseDTO.setTicketPurchaseId(ticketPurchase.getTicketPurchaseId());
        ticketPurchaseDTO.setEventActivityId(eventActivity.getEventActivityId());
        ticketPurchaseDTO.setTicketId(ticket.getTicketId());
        ticketPurchaseDTO.setZoneId(zone.getZoneId());
        ticketPurchaseDTO.setSeatId(seat.getSeatId());
        ticketPurchaseDTO.setQrCode(ticketPurchase.getQrCode());
        ticketPurchaseDTO.setStatus(ticketPurchase.getStatus());
        ticketPurchaseDTO.setEventId(ticketPurchase.getEvent().getEventId());

        return ticketPurchaseDTO;
    }

    @Override
    public String checkinTicketPurchase(int checkinId) {
        CheckinLog checkinLog = checkinLogRepository
                .findById(checkinId)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_LOG_NOT_FOUND));

        TicketPurchase ticketPurchase = ticketPurchaseRepository
                .findById(checkinLog.getTicketPurchase().getTicketPurchaseId())
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

        EventActivity eventActivity = eventActivityRepository
                .findById(checkinLog.getTicketPurchase().getEventActivity().getEventActivityId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

        if (checkinLog.getCheckinStatus().equals(ECheckinLogStatus.PENDING.name())) {
            checkinLog.setCheckinTime(LocalDateTime.now());
            checkinLog.setCheckinStatus(ECheckinLogStatus.CHECKED_IN.name());
        }
        else if (checkinLog.getCheckinStatus().equals(ECheckinLogStatus.CHECKED_IN.name())) {
            throw new AppException(ErrorCode.CHECKIN_LOG_CHECKED_IN);
        }
        else if(eventActivity.getEndTimeEvent().isBefore(LocalTime.now())) {
            checkinLog.setCheckinTime(LocalDateTime.now());
            checkinLog.setCheckinStatus(ECheckinLogStatus.EXPIRED.name());
            throw new AppException(ErrorCode.CHECKIN_LOG_EXPIRED);
        }

        ticketPurchase.setStatus(ETicketPurchaseStatus.CHECKED_IN.name());
        ticketPurchaseRepository.save(ticketPurchase);
        checkinLogRepository.save(checkinLog);
        return "Checkin successful";
    }

    @Override
    public int countTotalTicketSold() {
        return ticketPurchaseRepository.countTotalTicketSold();
    }
}
