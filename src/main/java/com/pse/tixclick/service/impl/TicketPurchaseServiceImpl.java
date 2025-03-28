package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.*;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.CheckinLog;
import com.pse.tixclick.payload.entity.entity_enum.*;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.payment.Order;
import com.pse.tixclick.payload.entity.payment.OrderDetail;
import com.pse.tixclick.payload.entity.seatmap.*;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketMapping;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import com.pse.tixclick.payload.request.create.CheckinRequest;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;
import com.pse.tixclick.payload.request.create.ListTicketPurchaseRequest;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class TicketPurchaseServiceImpl implements TicketPurchaseService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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

    @Autowired
    TicketMappingRepository ticketMappingRepository;

    @Autowired
    ZoneActivityRepository zoneActivityRepository;

    @Autowired
    SeatActivityRepository seatActivityRepository;

    @Override
    public List<TicketPurchaseDTO> createTicketPurchase(ListTicketPurchaseRequest createTicketPurchaseRequest) {
        List<TicketPurchaseDTO> ticketPurchaseDTOList = new ArrayList<>();

        List<Integer> listTicketPurchase_id = new ArrayList<>();

        for(CreateTicketPurchaseRequest createTicketPurchaseRequest1 : createTicketPurchaseRequest.getTicketPurchaseRequests()){
            // Trường hợp không ghế và có zone
            if(createTicketPurchaseRequest1.getSeatId() == 0 && createTicketPurchaseRequest1.getZoneId() != 0){
                //Tạo TicketPurchase trước
                TicketPurchase ticketPurchase = new TicketPurchase();
                ticketPurchase.setStatus(ETicketPurchaseStatus.PENDING.name());

                Ticket ticket = ticketRepository.findById(createTicketPurchaseRequest1.getTicketId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

                Event event = eventRepository.findById(createTicketPurchaseRequest1.getEventId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

                ticketPurchase.setTicket(ticket);
                ticketPurchase.setEvent(event);
                ticketPurchase.setQrCode(null);
                ticketPurchase.setAccount(appUtils.getAccountFromAuthentication());
                ticketPurchase.setQuantity(createTicketPurchaseRequest1.getQuantity());

                //Kiểm tra ghế và zone
                TicketMapping ticketMapping = ticketMappingRepository
                        .findTicketMappingByTicketIdAndEventActivityId(createTicketPurchaseRequest1.getTicketId(), createTicketPurchaseRequest1.getEventActivityId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                if (ticketMapping.getQuantity() <= 0) {
                    throw new AppException(ErrorCode.TICKET_MAPPING_EMPTY);
                }
                if(ticketMapping.getQuantity() < createTicketPurchaseRequest1.getQuantity()){
                    throw new AppException(ErrorCode.TICKET_MAPPING_NOT_ENOUGH);
                }
                ticketMapping.setQuantity(ticketMapping.getQuantity() - createTicketPurchaseRequest1.getQuantity());
                ticketMappingRepository.save(ticketMapping);

                ZoneActivity zoneActivity = zoneActivityRepository
                        .findByEventActivityIdAndZoneId(createTicketPurchaseRequest1.getEventActivityId(), createTicketPurchaseRequest1.getZoneId())
                        .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                Zone zone = zoneRepository.findById(createTicketPurchaseRequest1.getZoneId())
                        .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));
                if (zoneActivity.getAvailableQuantity() <= 0) {
                    throw new AppException(ErrorCode.ZONE_FULL);
                }
                if(zoneActivity.getAvailableQuantity() < createTicketPurchaseRequest1.getQuantity()){
                    throw new AppException(ErrorCode.ZONE_NOT_ENOUGH);
                }
                zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() - createTicketPurchaseRequest1.getQuantity());
                if (zoneActivity.getAvailableQuantity() == 0) {
                    zone.setStatus(false);
                }
                zoneRepository.save(zone);
                zoneActivityRepository.save(zoneActivity);

                EventActivity eventActivity = eventActivityRepository.findById(createTicketPurchaseRequest1.getEventActivityId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

                ticketPurchase.setSeatActivity(null);
                ticketPurchase.setZoneActivity(zoneActivity);
                ticketPurchase.setEventActivity(eventActivity);
                ticketPurchase = ticketPurchaseRepository.save(ticketPurchase);

                TicketPurchaseDTO ticketPurchaseDTO = new TicketPurchaseDTO();
                ticketPurchaseDTO.setTicketPurchaseId(ticketPurchase.getTicketPurchaseId());
                ticketPurchaseDTO.setEventActivityId(eventActivity.getEventActivityId());
                ticketPurchaseDTO.setTicketId(ticket.getTicketId());
                ticketPurchaseDTO.setZoneId(zone.getZoneId());
                ticketPurchaseDTO.setSeatId(0);
                ticketPurchaseDTO.setQrCode(ticketPurchase.getQrCode());
                ticketPurchaseDTO.setStatus(ticketPurchase.getStatus());
                ticketPurchaseDTO.setEventId(ticketPurchase.getEvent().getEventId());
                ticketPurchaseDTO.setQuantity(createTicketPurchaseRequest1.getQuantity());

                ticketPurchaseDTOList.add(ticketPurchaseDTO);

                final int ticketPurchase_id = ticketPurchase.getTicketPurchaseId();
                listTicketPurchase_id.add(ticketPurchase_id);
            }
            // Trường hợp có ghế và có zone
            if(createTicketPurchaseRequest1.getZoneId() != 0 && createTicketPurchaseRequest1.getSeatId() != 0){
                TicketPurchase ticketPurchase = new TicketPurchase();
                ticketPurchase.setStatus(ETicketPurchaseStatus.PENDING.name());

                Ticket ticket = ticketRepository.findById(createTicketPurchaseRequest1.getTicketId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

                Event event = eventRepository.findById(createTicketPurchaseRequest1.getEventId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

                ticketPurchase.setTicket(ticket);
                ticketPurchase.setEvent(event);
                ticketPurchase.setQrCode(null);
                ticketPurchase.setAccount(appUtils.getAccountFromAuthentication());
                ticketPurchase.setQuantity(createTicketPurchaseRequest1.getQuantity());

                //Kiểm tra ghế và zone
                TicketMapping ticketMapping = ticketMappingRepository
                        .findTicketMappingByTicketIdAndEventActivityId(createTicketPurchaseRequest1.getTicketId(), createTicketPurchaseRequest1.getEventActivityId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                if (ticketMapping.getQuantity() <= 0) {
                    throw new AppException(ErrorCode.TICKET_MAPPING_EMPTY);
                }
                if(ticketMapping.getQuantity() < createTicketPurchaseRequest1.getQuantity()){
                    throw new AppException(ErrorCode.TICKET_MAPPING_NOT_ENOUGH);
                }
                ticketMapping.setQuantity(ticketMapping.getQuantity() - createTicketPurchaseRequest1.getQuantity());
                ticketMappingRepository.save(ticketMapping);

                ZoneActivity zoneActivity = zoneActivityRepository
                        .findByEventActivityIdAndZoneId(createTicketPurchaseRequest1.getEventActivityId(), createTicketPurchaseRequest1.getZoneId())
                        .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));
                Zone zone = zoneRepository.findById(createTicketPurchaseRequest1.getZoneId())
                        .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));
                if (zoneActivity.getAvailableQuantity() <= 0) {
                    throw new AppException(ErrorCode.ZONE_FULL);
                }
                if(zoneActivity.getAvailableQuantity() < createTicketPurchaseRequest1.getQuantity()){
                    throw new AppException(ErrorCode.ZONE_NOT_ENOUGH);
                }
                zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() - createTicketPurchaseRequest1.getQuantity());
                if (zoneActivity.getAvailableQuantity() == 0) {
                    zone.setStatus(false);
                }
                zoneRepository.save(zone);
                zoneActivityRepository.save(zoneActivity);

                SeatActivity seatActivity = seatActivityRepository
                        .findByEventActivityIdAndSeatId(createTicketPurchaseRequest1.getEventActivityId(), createTicketPurchaseRequest1.getSeatId())
                        .orElseThrow(() -> new AppException(ErrorCode.SEAT_ACTIVITY_NOT_FOUND));

                Seat seat = seatRepository.findById(createTicketPurchaseRequest1.getSeatId())
                        .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));
                if (seatActivity.getStatus().equals(ESeatActivityStatus.PENDING.name())) {
                    throw new AppException(ErrorCode.SEAT_NOT_AVAILABLE);
                }
                if(seatActivity.getStatus().equals(ESeatActivityStatus.SOLD.name())){
                    throw new AppException(ErrorCode.SEAT_SOLD_OUT);
                }
                seatActivity.setStatus(ESeatActivityStatus.PENDING.name());
                seatActivityRepository.save(seatActivity);

                EventActivity eventActivity = eventActivityRepository.findById(createTicketPurchaseRequest1.getEventActivityId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));


                ticketPurchase.setSeatActivity(seatActivity);
                ticketPurchase.setZoneActivity(zoneActivity);
                ticketPurchase.setEventActivity(eventActivity);
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
                ticketPurchaseDTO.setQuantity(createTicketPurchaseRequest1.getQuantity());

                ticketPurchaseDTOList.add(ticketPurchaseDTO);

                final Integer ticketPurchase_id = ticketPurchase.getTicketPurchaseId();
                listTicketPurchase_id.add(ticketPurchase_id);
            }
            // Trường hợp không ghế và không zone
            if(createTicketPurchaseRequest1.getZoneId() == 0 && createTicketPurchaseRequest1.getSeatId() == 0){
                TicketPurchase ticketPurchase = new TicketPurchase();
                ticketPurchase.setStatus(ETicketPurchaseStatus.PENDING.name());

                EventActivity eventActivity = eventActivityRepository.findById(createTicketPurchaseRequest1.getEventActivityId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

                Ticket ticket = ticketRepository.findById(createTicketPurchaseRequest1.getTicketId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

                Event event = eventRepository.findById(createTicketPurchaseRequest1.getEventId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

                TicketMapping ticketMapping = ticketMappingRepository
                        .findTicketMappingByTicketIdAndEventActivityId(createTicketPurchaseRequest1.getTicketId(), createTicketPurchaseRequest1.getEventActivityId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                if (ticketMapping.getQuantity() <= 0) {
                    throw new AppException(ErrorCode.TICKET_MAPPING_EMPTY);
                }
                if(ticketMapping.getQuantity() < createTicketPurchaseRequest1.getQuantity()){
                    throw new AppException(ErrorCode.TICKET_MAPPING_NOT_ENOUGH);
                }
                ticketMapping.setQuantity(ticketMapping.getQuantity() - createTicketPurchaseRequest1.getQuantity());
                ticketMappingRepository.save(ticketMapping);

                ticketPurchase.setTicket(ticket);
                ticketPurchase.setEvent(event);
                ticketPurchase.setQrCode(null);
                ticketPurchase.setAccount(appUtils.getAccountFromAuthentication());
                ticketPurchase.setQuantity(createTicketPurchaseRequest1.getQuantity());
                ticketPurchase.setSeatActivity(null);
                ticketPurchase.setZoneActivity(null);
                ticketPurchase.setEventActivity(eventActivity);
                ticketPurchase = ticketPurchaseRepository.save(ticketPurchase);

                TicketPurchaseDTO ticketPurchaseDTO = new TicketPurchaseDTO();
                ticketPurchaseDTO.setTicketPurchaseId(ticketPurchase.getTicketPurchaseId());
                ticketPurchaseDTO.setEventActivityId(eventActivity.getEventActivityId());
                ticketPurchaseDTO.setTicketId(ticket.getTicketId());
                ticketPurchaseDTO.setZoneId(0);
                ticketPurchaseDTO.setSeatId(0);
                ticketPurchaseDTO.setQrCode(ticketPurchase.getQrCode());
                ticketPurchaseDTO.setStatus(ticketPurchase.getStatus());
                ticketPurchaseDTO.setEventId(ticketPurchase.getEvent().getEventId());
                ticketPurchaseDTO.setQuantity(createTicketPurchaseRequest1.getQuantity());

                ticketPurchaseDTOList.add(ticketPurchaseDTO);

                final Integer ticketPurchase_id = ticketPurchase.getTicketPurchaseId();
                listTicketPurchase_id.add(ticketPurchase_id);
            }
        }
        CompletableFuture.runAsync(() -> scheduleStatusUpdate(LocalDateTime.now(), listTicketPurchase_id));

        return ticketPurchaseDTOList;
    }

    @Async
    public void scheduleStatusUpdate(LocalDateTime startTime, List<Integer> listTicketPurchase_id) {
        LocalDateTime currentTime = LocalDateTime.now();
        long delay = java.time.Duration.between(currentTime, startTime.plusMinutes(15)).toSeconds();

        if (delay > 0) {
            scheduler.schedule(() -> {
                for(Integer ticketPurchase_id : listTicketPurchase_id){
                    TicketPurchase ticketPurchase = ticketPurchaseRepository
                            .findById(ticketPurchase_id)
                            .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));
                    if (ticketPurchase.getStatus().equals(ETicketPurchaseStatus.PENDING.name())) {
                        //Nếu không ghế và có zone
                        if(ticketPurchase.getSeatActivity() == null && ticketPurchase.getZoneActivity() != null){
                            TicketMapping ticketMapping = ticketMappingRepository
                                    .findTicketMappingByTicketIdAndEventActivityId(ticketPurchase.getTicket().getTicketId(), ticketPurchase.getEventActivity().getEventActivityId())
                                    .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                            ticketMapping.setQuantity(ticketMapping.getQuantity() + ticketPurchase.getQuantity());
                            ticketMappingRepository.save(ticketMapping);

                            ZoneActivity zoneActivity = zoneActivityRepository
                                    .findByEventActivityIdAndZoneId(ticketPurchase.getZoneActivity().getEventActivity().getEventActivityId(), ticketPurchase.getZoneActivity().getZone().getZoneId())
                                    .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                            Zone zone = zoneRepository
                                    .findById(ticketPurchase.getZoneActivity().getZone().getZoneId())
                                    .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                            if (ticketPurchase.getZoneActivity().getAvailableQuantity() <= 0) {
                                zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() + ticketPurchase.getQuantity());
                                zone.setStatus(true);
                            } else {
                                zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() + ticketPurchase.getQuantity());
                            }
                            ticketPurchase.setStatus(ETicketPurchaseStatus.EXPIRED.name());

                            ticketPurchaseRepository.save(ticketPurchase);
                            zoneRepository.save(zone);
                            zoneActivityRepository.save(zoneActivity);
                        }

                        //Nếu có ghế và có zone
                        if(ticketPurchase.getZoneActivity() != null && ticketPurchase.getSeatActivity() != null){
                            TicketMapping ticketMapping = ticketMappingRepository
                                    .findTicketMappingByTicketIdAndEventActivityId(ticketPurchase.getTicket().getTicketId(), ticketPurchase.getEventActivity().getEventActivityId())
                                    .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                            ticketMapping.setQuantity(ticketMapping.getQuantity() + ticketPurchase.getQuantity());
                            ticketMappingRepository.save(ticketMapping);

                            SeatActivity seatActivity = seatActivityRepository
                                    .findByEventActivityIdAndSeatId(ticketPurchase.getSeatActivity().getEventActivity().getEventActivityId(), ticketPurchase.getSeatActivity().getSeat().getSeatId())
                                    .orElseThrow(() -> new AppException(ErrorCode.SEAT_ACTIVITY_NOT_FOUND));

                            seatActivity.setStatus(ESeatActivityStatus.AVAILABLE.name());

                            ZoneActivity zoneActivity = zoneActivityRepository
                                    .findByEventActivityIdAndZoneId(ticketPurchase.getZoneActivity().getEventActivity().getEventActivityId(), ticketPurchase.getZoneActivity().getZone().getZoneId())
                                    .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                            Zone zone = zoneRepository
                                    .findById(ticketPurchase.getZoneActivity().getZone().getZoneId())
                                    .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                            if (ticketPurchase.getZoneActivity().getAvailableQuantity() <= 0) {
                                zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() + ticketPurchase.getQuantity());
                                zone.setStatus(true);
                            } else {
                                zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() + ticketPurchase.getQuantity());
                            }

                            ticketPurchase.setStatus(ETicketPurchaseStatus.EXPIRED.name());

                            ticketPurchaseRepository.save(ticketPurchase);
                            zoneRepository.save(zone);
                            seatActivityRepository.save(seatActivity);
                        }

                        //Nếu không ghế và không zone
                        if(ticketPurchase.getZoneActivity() == null && ticketPurchase.getSeatActivity() == null){
                            TicketMapping ticketMapping = ticketMappingRepository
                                    .findTicketMappingByTicketIdAndEventActivityId(ticketPurchase.getTicket().getTicketId(), ticketPurchase.getEventActivity().getEventActivityId())
                                    .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                            ticketMapping.setQuantity(ticketMapping.getQuantity() + ticketPurchase.getQuantity());
                            ticketMappingRepository.save(ticketMapping);

                            ticketPurchase.setStatus(ETicketPurchaseStatus.EXPIRED.name());

                            ticketPurchaseRepository.save(ticketPurchase);
                        }
                }
                }
            }, delay, java.util.concurrent.TimeUnit.SECONDS);
        } else {
            for(Integer ticketPurchase_id : listTicketPurchase_id){
                TicketPurchase ticketPurchase = ticketPurchaseRepository
                        .findById(ticketPurchase_id)
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));
                if (ticketPurchase.getStatus().equals(ETicketPurchaseStatus.PENDING.name())) {
                    //Nếu không ghế và có zone
                    if(ticketPurchase.getSeatActivity() == null && ticketPurchase.getZoneActivity() != null){
                        TicketMapping ticketMapping = ticketMappingRepository
                                .findTicketMappingByTicketIdAndEventActivityId(ticketPurchase.getTicket().getTicketId(), ticketPurchase.getEventActivity().getEventActivityId())
                                .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                        ticketMapping.setQuantity(ticketMapping.getQuantity() + ticketPurchase.getQuantity());
                        ticketMappingRepository.save(ticketMapping);

                        ZoneActivity zoneActivity = zoneActivityRepository
                                .findByEventActivityIdAndZoneId(ticketPurchase.getZoneActivity().getEventActivity().getEventActivityId(), ticketPurchase.getZoneActivity().getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                        Zone zone = zoneRepository
                                .findById(ticketPurchase.getZoneActivity().getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                        if (ticketPurchase.getZoneActivity().getAvailableQuantity() <= 0) {
                            zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() + ticketPurchase.getQuantity());
                            zone.setStatus(true);
                        } else {
                            zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() + ticketPurchase.getQuantity());
                        }
                        ticketPurchase.setStatus(ETicketPurchaseStatus.EXPIRED.name());

                        ticketPurchaseRepository.save(ticketPurchase);
                        zoneRepository.save(zone);
                        zoneActivityRepository.save(zoneActivity);
                    }

                    //Nếu có ghế và có zone
                    if(ticketPurchase.getZoneActivity() != null && ticketPurchase.getSeatActivity() != null){
                        TicketMapping ticketMapping = ticketMappingRepository
                                .findTicketMappingByTicketIdAndEventActivityId(ticketPurchase.getTicket().getTicketId(), ticketPurchase.getEventActivity().getEventActivityId())
                                .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                        ticketMapping.setQuantity(ticketMapping.getQuantity() + ticketPurchase.getQuantity());
                        ticketMappingRepository.save(ticketMapping);

                        SeatActivity seatActivity = seatActivityRepository
                                .findByEventActivityIdAndSeatId(ticketPurchase.getSeatActivity().getEventActivity().getEventActivityId(), ticketPurchase.getSeatActivity().getSeat().getSeatId())
                                .orElseThrow(() -> new AppException(ErrorCode.SEAT_ACTIVITY_NOT_FOUND));

                        seatActivity.setStatus(ESeatActivityStatus.AVAILABLE.name());

                        ZoneActivity zoneActivity = zoneActivityRepository
                                .findByEventActivityIdAndZoneId(ticketPurchase.getZoneActivity().getEventActivity().getEventActivityId(), ticketPurchase.getZoneActivity().getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                        Zone zone = zoneRepository
                                .findById(ticketPurchase.getZoneActivity().getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                        if (ticketPurchase.getZoneActivity().getAvailableQuantity() <= 0) {
                            zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() + ticketPurchase.getQuantity());
                            zone.setStatus(true);
                        } else {
                            zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() + ticketPurchase.getQuantity());
                        }

                        ticketPurchase.setStatus(ETicketPurchaseStatus.EXPIRED.name());

                        ticketPurchaseRepository.save(ticketPurchase);
                        zoneRepository.save(zone);
                        seatActivityRepository.save(seatActivity);
                    }

                    //Nếu không ghế và không zone
                    if(ticketPurchase.getZoneActivity() == null && ticketPurchase.getSeatActivity() == null){
                        TicketMapping ticketMapping = ticketMappingRepository
                                .findTicketMappingByTicketIdAndEventActivityId(ticketPurchase.getTicket().getTicketId(), ticketPurchase.getEventActivity().getEventActivityId())
                                .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                        ticketMapping.setQuantity(ticketMapping.getQuantity() + ticketPurchase.getQuantity());
                        ticketMappingRepository.save(ticketMapping);

                        ticketPurchase.setStatus(ETicketPurchaseStatus.EXPIRED.name());

                        ticketPurchaseRepository.save(ticketPurchase);
                    }
                }
            }
        }
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
                .findById(ticketPurchase.getEventActivity().getEventActivityId())
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
        return Optional.of(ticketPurchaseRepository.countTotalTicketSold()).orElse(0);
    }

    @Override
    public List<TicketSalesResponse> getMonthlyTicketSales() {
        List<Object[]> results = ticketPurchaseRepository.countTicketsSoldPerMonth();
        return results.stream()
                .map(row -> new TicketSalesResponse(
                        ((Number) row[0]).intValue(),  // month
                        ((Number) row[1]).intValue()   // total_tickets_sold
                ))
                .collect(Collectors.toList());
    }

    @Override
    public int countTotalCheckins() {
        return Optional.of(checkinLogRepository.countTotalCheckins()).orElse(0);
    }

    @Override
    public TicketsSoldAndRevenueDTO getTicketsSoldAndRevenueByDay(int days) {
        List<Object[]> results = ticketPurchaseRepository.countTicketsSoldAndRevenueByDay(days);

        if (results.isEmpty()) {
            return new TicketsSoldAndRevenueDTO(days, 0, 0, 0, 0, 0);
        }

        double totalRevenue = 0;
        int totalTicketsSold = 0;
        int totalEvents = 0;

        for (Object[] row : results) {
            // row[0] là java.sql.Date, không phải số -> BỎ QUA nó
            totalRevenue += ((Number) row[1]).doubleValue();
            totalTicketsSold += ((Number) row[2]).intValue();
            totalEvents += ((Number) row[3]).intValue();
        }
        LocalDate earliestDate = LocalDate.now().minusDays(30);

        List<Object[]> results2 = ticketPurchaseRepository.countTicketsSoldAndRevenueByDayAfter(days, earliestDate);

        double totalRevenue2 = 0;
        int totalTicketsSold2 = 0;
        int totalEvents2 = 0;

        for (Object[] row : results2) {
            // row[0] là java.sql.Date, không phải số -> BỎ QUA nó
            totalRevenue2 += ((Number) row[1]).doubleValue();
            totalTicketsSold2 += ((Number) row[2]).intValue();
            totalEvents2 += ((Number) row[3]).intValue();
        }
        double avgDailyRevenue = totalRevenue / days;
        double revenueGrowth = 0;

        if (totalRevenue2 > 0) {
            revenueGrowth = ((totalRevenue - totalRevenue2) / totalRevenue2) * 100;
        } else if (totalRevenue > 0) {
            revenueGrowth = 100;
        } else {
            revenueGrowth = 0;
        }

        return new TicketsSoldAndRevenueDTO(days, totalTicketsSold, totalRevenue, totalEvents, avgDailyRevenue, revenueGrowth);
    }

    @Override
    public List<MyTicketDTO> getTicketPurchasesByAccount() {
        List<TicketPurchase> ticketPurchases = ticketPurchaseRepository.getTicketPurchasesByAccount(appUtils.getAccountFromAuthentication().getAccountId());
        if (ticketPurchases.isEmpty()) {
            return null;
        }
        List<MyTicketDTO> myTicketDTOS = ticketPurchases.stream()
                .map(myTicket -> {
                    MyTicketDTO myTicketDTO = modelMapper.map(myTicket, MyTicketDTO.class);

                    if (myTicket.getEvent() != null) {
                        myTicketDTO.setEventName(myTicket.getEvent().getEventName());
                    }
//                    if (myTicket.getActivityAssignments() != null) {
//                        myTicketDTO.setEventDate(myTicket.getEventActivity().getDateEvent());
//                    }
//                    if (myTicket.getEventActivity() != null) {
//                        myTicketDTO.setEventStartTime(myTicket.getEventActivity().getStartTimeEvent());
//                    }
                    if (myTicket.getEvent() != null) {
                        myTicketDTO.setLocation(myTicket.getEvent().getLocation());
                    }
                    if (myTicket.getTicket() != null) {
                        myTicketDTO.setPrice(myTicket.getTicket().getPrice());
                    }
                    if (myTicket.getTicket() != null) {
                        myTicketDTO.setTicketType(myTicket.getTicket().getTicketName());
                    }
                    return myTicketDTO;
                })
                .collect(Collectors.toList());

        return myTicketDTOS.isEmpty() ? null : myTicketDTOS;
    }

    @Override
    public TicketQrCodeDTO decryptQrCode(String qrCode) throws Exception {
        TicketQrCodeDTO ticketQrCodeDTO = AppUtils.decryptQrCode(qrCode);
        if (ticketQrCodeDTO == null) {
            throw new AppException(ErrorCode.QR_CODE_NOT_FOUND);
        }
        return ticketQrCodeDTO;
    }
}
