package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.*;
import com.pse.tixclick.payload.entity.CheckinLog;
import com.pse.tixclick.payload.entity.entity_enum.*;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.seatmap.*;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketMapping;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;
import com.pse.tixclick.payload.request.create.ListTicketPurchaseRequest;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.TicketPurchaseService;
import com.pse.tixclick.utils.AppUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class TicketPurchaseServiceImpl implements TicketPurchaseService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(50);

    @Override
    public int printActiveThreads() {
        if (scheduler instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) scheduler;
            int activeCount = executor.getActiveCount();
            System.out.println("Số lượng luồng đang hoạt động: " + activeCount);
            return activeCount;
        } else {
            System.out.println("Không thể lấy số lượng luồng đang hoạt động.");
        }
        return 0;
    }

    @Autowired
    AppUtils appUtils;

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
    CheckinLogRepository checkinLogRepository;

    @Autowired
    TicketMappingRepository ticketMappingRepository;

    @Autowired
    ZoneActivityRepository zoneActivityRepository;

    @Autowired
    SeatActivityRepository seatActivityRepository;

    @Autowired
    SimpMessagingTemplate messagingTemplate;


    @Override
    public List<TicketPurchaseDTO> createTicketPurchase(ListTicketPurchaseRequest createTicketPurchaseRequest) {
        if (!appUtils.getAccountFromAuthentication().getRole().getRoleName().equals(ERole.BUYER) &&
                !appUtils.getAccountFromAuthentication().getRole().getRoleName().equals(ERole.ORGANIZER)) {
            throw new AppException(ErrorCode.NOT_PERMISSION);
        }
        Optional<Event> eventOptional = eventRepository
                .findById(createTicketPurchaseRequest.getTicketPurchaseRequests().stream().findFirst().get().getEventId());
        if(eventOptional.get().getOrganizer().getAccountId() == (appUtils.getAccountFromAuthentication().getAccountId())){
            throw new AppException(ErrorCode.NOT_PERMISSION_ORGANIZER);
        }

        List<TicketPurchaseDTO> ticketPurchaseDTOList = new ArrayList<>();

        List<Integer> listTicketPurchase_id = new ArrayList<>();

        for(CreateTicketPurchaseRequest createTicketPurchaseRequest1 : createTicketPurchaseRequest.getTicketPurchaseRequests()){
            // Trường hợp không ghế và có zone
            if(createTicketPurchaseRequest1.getSeatId() == 0 && createTicketPurchaseRequest1.getZoneId() != 0){
                //Kiêm tra ticket
                Ticket ticket = ticketRepository
                        .findById(createTicketPurchaseRequest1.getTicketId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

                int quantity = createTicketPurchaseRequest1.getQuantity();
                int minQuantity = ticket.getMinQuantity();
                int maxQuantity = ticket.getMaxQuantity();

                if (quantity < minQuantity || quantity > maxQuantity) {
                    throw new AppException(ErrorCode.INVALID_QUANTITY);
                }

                Event event = eventRepository.findById(createTicketPurchaseRequest1.getEventId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

                EventActivity eventActivity = eventActivityRepository.findById(createTicketPurchaseRequest1.getEventActivityId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

                //Kiểm tra zone
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

                //Taọ TicketPurchase
                TicketPurchase ticketPurchase = new TicketPurchase();
                ticketPurchase.setTicket(ticket);
                ticketPurchase.setEvent(event);
                ticketPurchase.setQrCode(null);
                ticketPurchase.setAccount(appUtils.getAccountFromAuthentication());
                ticketPurchase.setQuantity(createTicketPurchaseRequest1.getQuantity());
                ticketPurchase.setStatus(ETicketPurchaseStatus.PENDING);
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
                //Kiểm tra ticket
                Ticket ticket = ticketRepository.findById(createTicketPurchaseRequest1.getTicketId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

                int quantity = createTicketPurchaseRequest1.getQuantity();
                int minQuantity = ticket.getMinQuantity();
                int maxQuantity = ticket.getMaxQuantity();

                if (quantity < minQuantity || quantity > maxQuantity) {
                    throw new AppException(ErrorCode.INVALID_QUANTITY);
                }

                Event event = eventRepository.findById(createTicketPurchaseRequest1.getEventId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

                EventActivity eventActivity = eventActivityRepository.findById(createTicketPurchaseRequest1.getEventActivityId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

                //Kiểm tra zone
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
                zoneActivityRepository.save(zoneActivity);

                //Kiểm tra seat
                SeatActivity seatActivity = seatActivityRepository
                        .findByEventActivityIdAndSeatId(createTicketPurchaseRequest1.getEventActivityId(), createTicketPurchaseRequest1.getSeatId())
                        .orElseThrow(() -> new AppException(ErrorCode.SEAT_ACTIVITY_NOT_FOUND));

                Seat seat = seatRepository.findById(createTicketPurchaseRequest1.getSeatId())
                        .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));
                if (seatActivity.getStatus().equals(ESeatActivityStatus.PENDING)) {
                    throw new AppException(ErrorCode.SEAT_NOT_AVAILABLE);
                }
                if(seatActivity.getStatus().equals(ESeatActivityStatus.SOLD)){
                    throw new AppException(ErrorCode.SEAT_SOLD_OUT);
                }
                seatActivity.setStatus(ESeatActivityStatus.PENDING);
                seatActivityRepository.save(seatActivity);

                //Tạo TicketPurchase
                TicketPurchase ticketPurchase = new TicketPurchase();
                ticketPurchase.setStatus(ETicketPurchaseStatus.PENDING);
                ticketPurchase.setTicket(ticket);
                ticketPurchase.setEvent(event);
                ticketPurchase.setQrCode(null);
                ticketPurchase.setAccount(appUtils.getAccountFromAuthentication());
                ticketPurchase.setQuantity(createTicketPurchaseRequest1.getQuantity());
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

                //Kiểm tra ticket
                Ticket ticket = ticketRepository.findById(createTicketPurchaseRequest1.getTicketId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

                int quantity = createTicketPurchaseRequest1.getQuantity();
                int minQuantity = ticket.getMinQuantity();
                int maxQuantity = ticket.getMaxQuantity();

                if (quantity < minQuantity || quantity > maxQuantity) {
                    throw new AppException(ErrorCode.INVALID_QUANTITY);
                }
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
                if (ticketMapping.getQuantity() == 0) {
                    ticketMapping.setStatus(false);
                }
                ticketMappingRepository.save(ticketMapping);

                Event event = eventRepository.findById(createTicketPurchaseRequest1.getEventId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

                EventActivity eventActivity = eventActivityRepository.findById(createTicketPurchaseRequest1.getEventActivityId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

                //Tạo TicketPurchase
                TicketPurchase ticketPurchase = new TicketPurchase();
                ticketPurchase.setStatus(ETicketPurchaseStatus.PENDING);
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
        String code = String.valueOf(listTicketPurchase_id.get(0));
        if (delay >= 0) {
            // Gửi countdown đến WebSocket mỗi giây
            ScheduledFuture<?> countdownTask = scheduler.scheduleAtFixedRate(() -> {
                long remainingTime = java.time.Duration.between(LocalDateTime.now(), startTime.plusMinutes(15)).toSeconds();
                if (remainingTime >= 0) {
                    sendExpiredTime(remainingTime, code);
                }
            }, 0, 1, TimeUnit.SECONDS); // Chạy ngay lập tức, lặp lại mỗi giây

            // Khi countdown kết thúc, cập nhật trạng thái
            scheduler.schedule(() -> {
                countdownTask.cancel(false);
                // Cập nhật trạng thái của ticketPurchase
                updateTicketPurchaseStatus(listTicketPurchase_id);
            }, delay, TimeUnit.SECONDS); // Sử dụng đúng đơn vị thời gian
        } else {
            // Nếu delay <= 0, cập nhật ngay lập tức
            updateTicketPurchaseStatus(listTicketPurchase_id);
        }
    }

    public void sendExpiredTime(long expiredTime, String websocketChannel) {
        try {
            // Gửi thông điệp đến cổng WebSocket riêng biệt cho từng luồng
            messagingTemplate.convertAndSend("/all/" + websocketChannel + "/ticket-purchase-expired",
                    Map.of("expiredTime", expiredTime));
            System.out.println("✅ Time left: " + expiredTime + " for channel: " + websocketChannel);
        } catch (Exception e) {
            System.err.println("❌ Error sending WebSocket message: " + e.getMessage());
        }
    }

    @Override
    public String cancelTicketPurchase(List<Integer> ticketPurchaseIds) {
        for(Integer ticketPurchase_id : ticketPurchaseIds){
            TicketPurchase ticketPurchase = ticketPurchaseRepository
                    .findById(ticketPurchase_id)
                    .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));
            //Kiểm tra trạng thái của ticketPurchase xem thanh toán hay huỷ nếu không thì đi xuống dưới
            if (ticketPurchase.getStatus().equals(ETicketPurchaseStatus.PENDING)) {
                //Nếu không ghế và có zone
                if(ticketPurchase.getSeatActivity() == null && ticketPurchase.getZoneActivity() != null){
                    ZoneActivity zoneActivity = zoneActivityRepository
                            .findByEventActivityIdAndZoneId(ticketPurchase.getZoneActivity().getEventActivity().getEventActivityId(), ticketPurchase.getZoneActivity().getZone().getZoneId())
                            .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                    Zone zone = zoneRepository
                            .findById(ticketPurchase.getZoneActivity().getZone().getZoneId())
                            .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                    zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() + ticketPurchase.getQuantity());

                    ticketPurchase.setStatus(ETicketPurchaseStatus.CANCELLED);

                    ticketPurchaseRepository.save(ticketPurchase);
                    zoneRepository.save(zone);
                    zoneActivityRepository.save(zoneActivity);
                }

                //Nếu có ghế và có zone
                if(ticketPurchase.getZoneActivity() != null && ticketPurchase.getSeatActivity() != null){
                    SeatActivity seatActivity = seatActivityRepository
                            .findByEventActivityIdAndSeatId(ticketPurchase.getSeatActivity().getEventActivity().getEventActivityId(), ticketPurchase.getSeatActivity().getSeat().getSeatId())
                            .orElseThrow(() -> new AppException(ErrorCode.SEAT_ACTIVITY_NOT_FOUND));

                    seatActivity.setStatus(ESeatActivityStatus.AVAILABLE);

                    ZoneActivity zoneActivity = zoneActivityRepository
                            .findByEventActivityIdAndZoneId(ticketPurchase.getZoneActivity().getEventActivity().getEventActivityId(), ticketPurchase.getZoneActivity().getZone().getZoneId())
                            .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                    Zone zone = zoneRepository
                            .findById(ticketPurchase.getZoneActivity().getZone().getZoneId())
                            .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                    zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() + ticketPurchase.getQuantity());

                    ticketPurchase.setStatus(ETicketPurchaseStatus.CANCELLED);

                    zoneActivityRepository.save(zoneActivity);
                    ticketPurchaseRepository.save(ticketPurchase);
                    zoneRepository.save(zone);
                    seatActivityRepository.save(seatActivity);
                }

                //Nếu không ghế và không zone
                if(ticketPurchase.getZoneActivity() == null && ticketPurchase.getSeatActivity() == null){
                    TicketMapping ticketMapping = ticketMappingRepository
                            .findTicketMappingByTicketIdAndEventActivityId(ticketPurchase.getTicket().getTicketId(), ticketPurchase.getEventActivity().getEventActivityId())
                            .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                    if (ticketMapping.getQuantity() == 0) {
                        ticketMapping.setQuantity(ticketPurchase.getQuantity());
                        ticketMapping.setStatus(true);
                    }
                    else {
                        ticketMapping.setQuantity(ticketMapping.getQuantity() + ticketPurchase.getQuantity());
                    }

                    ticketMappingRepository.save(ticketMapping);

                    ticketPurchase.setStatus(ETicketPurchaseStatus.CANCELLED);

                    ticketPurchaseRepository.save(ticketPurchase);
                }
            }
        }
        return "Cancel ticket purchase successfully";
    }

    @Override
    public MyTicketDTO getTicketPurchaseById(int ticketPurchaseId) {
        var context = SecurityContextHolder.getContext();
        var userName = context.getAuthentication().getName();

        TicketPurchase myTicket = ticketPurchaseRepository
                .findByTicketPurchaseIdAndStatusAndAccount_UserName(
                        ticketPurchaseId,
                        ETicketPurchaseStatus.PURCHASED,
                        userName
                )
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

        MyTicketDTO myTicketDTO = new MyTicketDTO();

        if (myTicket.getEvent() != null) {
            myTicketDTO.setEventId(myTicket.getEvent().getEventId());
            myTicketDTO.setEventName(myTicket.getEvent().getEventName());
            myTicketDTO.setEventActivityId(myTicket.getEventActivity().getEventActivityId());
            myTicketDTO.setTicketPurchaseId(myTicket.getTicketPurchaseId());
            myTicketDTO.setLogo(myTicket.getEvent().getLogoURL());
            myTicketDTO.setBanner(myTicket.getEvent().getBannerURL());
            myTicketDTO.setIshaveSeatmap(myTicket.getEvent().getSeatMap() != null);

            StringBuilder locationBuilder = new StringBuilder();
            if(myTicket.getEvent().getAddress() != null) {
                locationBuilder.append(myTicket.getEvent().getAddress()).append(", ");
            }
            if (myTicket.getEvent().getWard() != null) {
                locationBuilder.append(myTicket.getEvent().getWard()).append(", ");
            }
            if (myTicket.getEvent().getDistrict() != null) {
                locationBuilder.append(myTicket.getEvent().getDistrict()).append(", ");
            }
            if (myTicket.getEvent().getCity() != null) {
                locationBuilder.append(myTicket.getEvent().getCity());
            }
            if (locationBuilder.length() > 0) {
                if (locationBuilder.lastIndexOf(", ") == locationBuilder.length() - 2) {
                    locationBuilder.delete(locationBuilder.length() - 2, locationBuilder.length());
                }
                myTicketDTO.setLocation(locationBuilder.toString());
            } else {
                myTicketDTO.setLocation(null);
            }
        }

        if (myTicket.getEventActivity() != null) {
            myTicketDTO.setEventDate(myTicket.getEventActivity().getDateEvent());
            myTicketDTO.setEventStartTime(myTicket.getEventActivity().getStartTimeEvent());
        }

        if (myTicket.getTicket() != null) {
            myTicketDTO.setPrice(myTicket.getTicket().getPrice());
            myTicketDTO.setTicketType(myTicket.getTicket().getTicketName());
        }

        if (myTicket.getZoneActivity() != null && myTicket.getZoneActivity().getZone() != null) {
            myTicketDTO.setZoneName(myTicket.getZoneActivity().getZone().getZoneName());
        }

        if (myTicket.getSeatActivity() != null && myTicket.getSeatActivity().getSeat() != null) {
            myTicketDTO.setSeatCode(myTicket.getSeatActivity().getSeat().getSeatName());
        }

        myTicketDTO.setQuantity(myTicket.getQuantity());
        myTicketDTO.setQrCode(myTicket.getQrCode());

        return myTicketDTO;
    }


    private void updateTicketPurchaseStatus(List<Integer> listTicketPurchaseId){
        for(Integer ticketPurchase_id : listTicketPurchaseId){
            TicketPurchase ticketPurchase = ticketPurchaseRepository
                    .findById(ticketPurchase_id)
                    .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));
            //Kiểm tra trạng thái của ticketPurchase xem thanh toán hay huỷ nếu không thì đi xuống dưới
            if (ticketPurchase.getStatus().equals(ETicketPurchaseStatus.PENDING)) {
                //Nếu không ghế và có zone
                if(ticketPurchase.getSeatActivity() == null && ticketPurchase.getZoneActivity() != null){
                    ZoneActivity zoneActivity = zoneActivityRepository
                            .findByEventActivityIdAndZoneId(ticketPurchase.getZoneActivity().getEventActivity().getEventActivityId(), ticketPurchase.getZoneActivity().getZone().getZoneId())
                            .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                    Zone zone = zoneRepository
                            .findById(ticketPurchase.getZoneActivity().getZone().getZoneId())
                            .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                    zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() + ticketPurchase.getQuantity());

                    ticketPurchase.setStatus(ETicketPurchaseStatus.EXPIRED);

                    ticketPurchaseRepository.save(ticketPurchase);
                    zoneRepository.save(zone);
                    zoneActivityRepository.save(zoneActivity);
                }

                //Nếu có ghế và có zone
                if(ticketPurchase.getZoneActivity() != null && ticketPurchase.getSeatActivity() != null){
                    SeatActivity seatActivity = seatActivityRepository
                            .findByEventActivityIdAndSeatId(ticketPurchase.getSeatActivity().getEventActivity().getEventActivityId(), ticketPurchase.getSeatActivity().getSeat().getSeatId())
                            .orElseThrow(() -> new AppException(ErrorCode.SEAT_ACTIVITY_NOT_FOUND));

                    seatActivity.setStatus(ESeatActivityStatus.AVAILABLE);

                    ZoneActivity zoneActivity = zoneActivityRepository
                            .findByEventActivityIdAndZoneId(ticketPurchase.getZoneActivity().getEventActivity().getEventActivityId(), ticketPurchase.getZoneActivity().getZone().getZoneId())
                            .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                    Zone zone = zoneRepository
                            .findById(ticketPurchase.getZoneActivity().getZone().getZoneId())
                            .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                    zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() + ticketPurchase.getQuantity());

                    ticketPurchase.setStatus(ETicketPurchaseStatus.EXPIRED);

                    zoneActivityRepository.save(zoneActivity);
                    ticketPurchaseRepository.save(ticketPurchase);
                    zoneRepository.save(zone);
                    seatActivityRepository.save(seatActivity);
                }

                //Nếu không ghế và không zone
                if(ticketPurchase.getZoneActivity() == null && ticketPurchase.getSeatActivity() == null){
                    TicketMapping ticketMapping = ticketMappingRepository
                            .findTicketMappingByTicketIdAndEventActivityId(ticketPurchase.getTicket().getTicketId(), ticketPurchase.getEventActivity().getEventActivityId())
                            .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                    if (ticketMapping.getQuantity() == 0) {
                        ticketMapping.setQuantity(ticketPurchase.getQuantity());
                        ticketMapping.setStatus(true);
                    }
                    else {
                        ticketMapping.setQuantity(ticketMapping.getQuantity() + ticketPurchase.getQuantity());
                    }

                    ticketMappingRepository.save(ticketMapping);

                    ticketPurchase.setStatus(ETicketPurchaseStatus.EXPIRED);

                    ticketPurchaseRepository.save(ticketPurchase);
                }
            }
        }
    }
    @Override
    public String checkinTicketPurchase(int checkinId) {
        if (!appUtils.getAccountFromAuthentication().getRole().getRoleName().equals(ERole.ORGANIZER)) {
            throw new AppException(ErrorCode.NOT_PERMISSION);
        }
        CheckinLog checkinLog = checkinLogRepository
                .findById(checkinId)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_LOG_NOT_FOUND));

        TicketPurchase ticketPurchase = ticketPurchaseRepository
                .findById(checkinLog.getTicketPurchase().getTicketPurchaseId())
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

        EventActivity eventActivity = eventActivityRepository
                .findById(ticketPurchase.getEventActivity().getEventActivityId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

        if (checkinLog.getCheckinStatus().equals(ECheckinLogStatus.PENDING)) {
            checkinLog.setCheckinTime(LocalDateTime.now());
            checkinLog.setCheckinStatus(ECheckinLogStatus.CHECKED_IN);
        }
        else if (checkinLog.getCheckinStatus().equals(ECheckinLogStatus.CHECKED_IN)) {
            throw new AppException(ErrorCode.CHECKIN_LOG_CHECKED_IN);
        }
        else if(eventActivity.getEndTimeEvent().isBefore(LocalTime.now())) {
            checkinLog.setCheckinTime(LocalDateTime.now());
            checkinLog.setCheckinStatus(ECheckinLogStatus.EXPIRED);
            throw new AppException(ErrorCode.CHECKIN_LOG_EXPIRED);
        }

        ticketPurchase.setStatus(ETicketPurchaseStatus.CHECKED_IN);
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
        printActiveThreads();
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
        if (!appUtils.getAccountFromAuthentication().getRole().getRoleName().equals(ERole.BUYER) && !
                appUtils.getAccountFromAuthentication().getRole().getRoleName().equals(ERole.ORGANIZER)) {
            throw new AppException(ErrorCode.NOT_PERMISSION);
        }

        List<TicketPurchase> ticketPurchases = ticketPurchaseRepository
                .getTicketPurchasesByAccount(appUtils.getAccountFromAuthentication().getAccountId());

        if (ticketPurchases.isEmpty()) {
            return null;
        }

        List<MyTicketDTO> myTicketDTOS = ticketPurchases.stream()
                .filter(ticketPurchase -> ticketPurchase.getStatus().equals(ETicketPurchaseStatus.PURCHASED))
                .map(myTicket -> {
                    MyTicketDTO myTicketDTO = new MyTicketDTO();

                    if (myTicket.getEvent() != null) {
                        myTicketDTO.setEventId(myTicket.getEvent().getEventId());
                        myTicketDTO.setEventName(myTicket.getEvent().getEventName());
                        myTicketDTO.setEventActivityId(myTicket.getEventActivity().getEventActivityId());
                        myTicketDTO.setLocationName(myTicket.getEvent().getLocationName());
                        myTicketDTO.setTicketPurchaseId(myTicket.getTicketPurchaseId());
                        myTicketDTO.setLogo(myTicket.getEvent().getLogoURL());
                        myTicketDTO.setBanner(myTicket.getEvent().getBannerURL());
                        if (myTicket.getEvent().getSeatMap() != null) {
                            myTicketDTO.setIshaveSeatmap(true);
                        } else {
                            myTicketDTO.setIshaveSeatmap(false);
                        }

                        StringBuilder locationBuilder = new StringBuilder();
                        if(myTicket.getEvent().getAddress() != null) {
                            locationBuilder.append(myTicket.getEvent().getAddress()).append(", ");
                        }
                        if (myTicket.getEvent().getWard() != null) {
                            locationBuilder.append(myTicket.getEvent().getWard()).append(", ");
                        }
                        if (myTicket.getEvent().getDistrict() != null) {
                            locationBuilder.append(myTicket.getEvent().getDistrict()).append(", ");
                        }
                        if (myTicket.getEvent().getCity() != null) {
                            locationBuilder.append(myTicket.getEvent().getCity());
                        }
                        if (locationBuilder.length() > 0) {
                            if (locationBuilder.lastIndexOf(", ") == locationBuilder.length() - 2) {
                                locationBuilder.delete(locationBuilder.length() - 2, locationBuilder.length());
                            }

                            myTicketDTO.setLocation(locationBuilder.toString());
                        } else {
                            myTicketDTO.setLocation(null);
                        }
                    }
                    if (myTicket.getEventActivity() != null) {
                        myTicketDTO.setEventDate(myTicket.getEventActivity().getDateEvent());
                        myTicketDTO.setEventStartTime(myTicket.getEventActivity().getStartTimeEvent());
                    }
                    if (myTicket.getTicket() != null) {
                        myTicketDTO.setPrice(myTicket.getTicket().getPrice());
                        myTicketDTO.setTicketType(myTicket.getTicket().getTicketName());
                    }
                    if (myTicket.getZoneActivity() != null && myTicket.getZoneActivity().getZone() != null) {
                        myTicketDTO.setZoneName(myTicket.getZoneActivity().getZone().getZoneName());
                    }
                    if (myTicket.getSeatActivity() != null && myTicket.getSeatActivity().getSeat() != null) {
                        myTicketDTO.setSeatCode(myTicket.getSeatActivity().getSeat().getSeatName());
                    }

                    myTicketDTO.setQuantity(myTicket.getQuantity());
                    myTicketDTO.setQrCode(myTicket.getQrCode());
                    return myTicketDTO;
                })
                .collect(Collectors.toList());

        return myTicketDTOS.isEmpty() ? null : myTicketDTOS;
    }


    @Override
    public TicketQrCodeDTO decryptQrCode(String qrCode){
        if (!appUtils.getAccountFromAuthentication().getRole().getRoleName().equals(ERole.ORGANIZER)) {
            throw new AppException(ErrorCode.NOT_PERMISSION);
        }

        TicketQrCodeDTO ticketQrCodeDTO = AppUtils.decryptQrCode(qrCode);
        if (ticketQrCodeDTO == null) {
            throw new AppException(ErrorCode.QR_CODE_NOT_FOUND);
        }


        CheckinLog checkinLog = checkinLogRepository
                .findById(ticketQrCodeDTO.getCheckin_Log_id())
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_LOG_NOT_FOUND));

        TicketPurchase ticketPurchase = ticketPurchaseRepository
                .findById(checkinLog.getTicketPurchase().getTicketPurchaseId())
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

        EventActivity eventActivity = eventActivityRepository
                .findById(ticketPurchase.getEventActivity().getEventActivityId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

        if (checkinLog.getCheckinStatus().equals(ECheckinLogStatus.PENDING)) {
            checkinLog.setCheckinTime(LocalDateTime.now());
            checkinLog.setCheckinStatus(ECheckinLogStatus.CHECKED_IN);
        }
        else if (checkinLog.getCheckinStatus().equals(ECheckinLogStatus.CHECKED_IN)) {
            throw new AppException(ErrorCode.CHECKIN_LOG_CHECKED_IN);
        }
        else if(eventActivity.getEndTimeEvent().isBefore(LocalTime.now())) {
            checkinLog.setCheckinTime(LocalDateTime.now());
            checkinLog.setCheckinStatus(ECheckinLogStatus.EXPIRED);
            throw new AppException(ErrorCode.CHECKIN_LOG_EXPIRED);
        }

        ticketPurchase.setStatus(ETicketPurchaseStatus.CHECKED_IN);
        ticketPurchaseRepository.save(ticketPurchase);
        checkinLogRepository.save(checkinLog);
        ticketQrCodeDTO.setStatus(ticketPurchase.getStatus().name());
        return ticketQrCodeDTO;
    }

    @Override
    public int countTicketPurchaseStatusByPurchased() {
        var count = ticketPurchaseRepository.countTicketPurchasesByStatus(ETicketPurchaseStatus.PURCHASED);
        return count;
    }
}
