package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.dto.TicketDTO;
import com.pse.tixclick.payload.dto.TicketPurchaseDTO;
import com.pse.tixclick.payload.dto.TicketQrCodeDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.entity_enum.ETicketPurchaseStatus;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
        ticketPurchase.setStatus(ETicketPurchaseStatus.PENDING.name());

        // Lấy thông tin Zone
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

        // Lấy thông tin Seat
        Seat seat = seatRepository.findById(createTicketPurchaseRequest.getSeatId())
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));

        if (!seat.isStatus()) {
            throw new AppException(ErrorCode.SEAT_NOT_AVAILABLE);
        }
        seat.setStatus(false);
        seatRepository.save(seat);

        // Lấy thông tin EventActivity, Ticket, Event
        EventActivity eventActivity = eventActivityRepository.findById(createTicketPurchaseRequest.getEventActivityId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

        Ticket ticket = ticketRepository.findById(createTicketPurchaseRequest.getTicketId())
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

        Event event = eventRepository.findById(createTicketPurchaseRequest.getEventId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // Gán dữ liệu vào ticketPurchase
        ticketPurchase.setZone(zone);
        ticketPurchase.setSeat(seat);
        ticketPurchase.setEventActivity(eventActivity);
        ticketPurchase.setTicket(ticket);
        ticketPurchase.setEvent(event);

        // Tạo dữ liệu QR Code
        Account account = appUtils.getAccountFromAuthentication();

        TicketQrCodeDTO ticketQrCodeDTO = new TicketQrCodeDTO();
        ticketQrCodeDTO.setTicket_name(ticket.getTicketName());
        ticketQrCodeDTO.setPurchase_date(new Date());
        ticketQrCodeDTO.setEvent_name(event.getEventName());
        ticketQrCodeDTO.setActivity_name(eventActivity.getActivityName());
        ticketQrCodeDTO.setZone_name(zone.getZoneName());
        ticketQrCodeDTO.setSeat_row_number(seat.getRowNumber());
        ticketQrCodeDTO.setSeat_column_number(seat.getColumnNumber());
        ticketQrCodeDTO.setAccount_name(account.getUserName());
        ticketQrCodeDTO.setPhone(account.getPhone());

        String qrCode = generateQRCode(ticketQrCodeDTO);
        ticketPurchase.setQrCode(qrCode);

        // Lưu vào database
        ticketPurchase = ticketPurchaseRepository.save(ticketPurchase);

        // Tạo DTO (explicit mapping để tránh lỗi ModelMapper)
        TicketPurchaseDTO ticketPurchaseDTO = new TicketPurchaseDTO();
        ticketPurchaseDTO.setTicketPurchaseId(ticketPurchase.getTicketPurchaseId());
        ticketPurchaseDTO.setEventActivityId(eventActivity.getEventActivityId()); // Chỉ lấy đúng ID, tránh xung đột với event
        ticketPurchaseDTO.setTicketId(ticket.getTicketId()); // Chỉ lấy đúng ID, tránh xung đột với ticketPurchaseId
        ticketPurchaseDTO.setZoneId(zone.getZoneId());
        ticketPurchaseDTO.setSeatId(seat.getSeatId());
        ticketPurchaseDTO.setQrCode(ticketPurchase.getQrCode());
        ticketPurchaseDTO.setStatus(ticketPurchase.getStatus());
        ticketPurchaseDTO.setEventId(ticketPurchase.getEvent().getEventId());

        return ticketPurchaseDTO;
    }



    private String generateQRCode(TicketQrCodeDTO ticketQrCodeDTO) {
        String dataTransfer = AppUtils.transferToString(ticketQrCodeDTO);
        return AppUtils.generateQRCode(dataTransfer, QR_CODE_WIDTH, QR_CODE_HEIGHT);
    }
}
