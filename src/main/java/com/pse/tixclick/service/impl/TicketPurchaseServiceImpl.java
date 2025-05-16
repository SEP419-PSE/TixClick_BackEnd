package com.pse.tixclick.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pse.tixclick.config.testnotification.Message;
import com.pse.tixclick.config.testnotification.MessageController;
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
import com.pse.tixclick.payload.request.QrCodeRequest;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;
import com.pse.tixclick.payload.request.create.ListTicketPurchaseRequest;
import com.pse.tixclick.payload.response.MyTicketResponse;
import com.pse.tixclick.payload.response.PaginationResponse;
import com.pse.tixclick.payload.response.TicketOwnerResponse;
import com.pse.tixclick.payload.response.TicketQRResponse;
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

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static com.nimbusds.jose.util.DeflateUtils.decompress;


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
    AccountRepository accountRepository;

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
    @Autowired
    MessageController messageController;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    OrderDetailRepository orderDetailRepository;

    @Autowired
    OrderRepository orderRepository;
    private static final String AES_ALGORITHM = "AES";
    private static final String SECRET_KEY = "0123456789abcdef";

    @Override
    public List<TicketPurchaseDTO> createTicketPurchase(ListTicketPurchaseRequest createTicketPurchaseRequest) throws Exception {
        AppUtils.checkRole(ERole.BUYER,ERole.ORGANIZER);
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

//                LocalDateTime currentDateTime = LocalDateTime.now(); // thời gian hiện tại
//                LocalDateTime saleStartDateTime = eventActivity.getStartTicketSale(); // thời gian bắt đầu bán vé
//
//                boolean hasSaleStarted = !saleStartDateTime.isAfter(currentDateTime); // sale đã bắt đầu nếu không sau thời điểm hiện tại
//
//                if (!hasSaleStarted) {
//                    throw new AppException(ErrorCode.TICKET_SALE_NOT_STARTED);
//                }


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
//                ticketPurchase.setQrCode(null);
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
//                ticketPurchaseDTO.setQrCode(ticketPurchase.getQrCode());
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
//                ticketPurchase.setQrCode(null);
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
//                ticketPurchaseDTO.setQrCode(ticketPurchase.getQrCode());
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
//                ticketPurchase.setQrCode(null);
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
//                ticketPurchaseDTO.setQrCode(ticketPurchase.getQrCode());
                ticketPurchaseDTO.setStatus(ticketPurchase.getStatus());
                ticketPurchaseDTO.setEventId(ticketPurchase.getEvent().getEventId());
                ticketPurchaseDTO.setQuantity(createTicketPurchaseRequest1.getQuantity());

                ticketPurchaseDTOList.add(ticketPurchaseDTO);

                final Integer ticketPurchase_id = ticketPurchase.getTicketPurchaseId();
                listTicketPurchase_id.add(ticketPurchase_id);
            }
        }
        CompletableFuture.runAsync(() -> scheduleStatusUpdate(LocalDateTime.now(), listTicketPurchase_id));

        simpMessagingTemplate.convertAndSend("/all/messages",
                "call api"); // Gửi Object Message

        return ticketPurchaseDTOList;
    }

    @Async
    public void scheduleStatusUpdate(LocalDateTime startTime, List<Integer> listTicketPurchase_id) {
        LocalDateTime currentTime = LocalDateTime.now();
        long delay = java.time.Duration.between(currentTime, startTime.plusMinutes(5)).toSeconds();
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
        simpMessagingTemplate.convertAndSend("/all/messages",
                "call api"); // Gửi Object Message
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
            myTicketDTO.setLocationName(myTicket.getEvent().getLocationName());

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
//        myTicketDTO.setQrCode(myTicket.getQrCode());

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
                    if(ticketPurchase.getTicketPurchaseOldId()!= null){
                        TicketPurchase ticketPurchaseOld = ticketPurchaseRepository
                                .findById(ticketPurchase.getTicketPurchaseOldId())
                                .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

                        ticketPurchaseOld.setStatus(ETicketPurchaseStatus.PURCHASED);
                        ticketPurchaseRepository.save(ticketPurchaseOld);


                        var zoneActivityOld = ticketPurchaseOld.getZoneActivity();
                        if (zoneActivityOld != null) {


                            zoneActivityOld.setAvailableQuantity(zoneActivityOld.getAvailableQuantity() - ticketPurchaseOld.getQuantity());
                            zoneActivityRepository.save(zoneActivityOld);
                        }


                    }
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
                    if(ticketPurchase.getTicketPurchaseOldId()!= null){
                        TicketPurchase ticketPurchaseOld = ticketPurchaseRepository
                                .findById(ticketPurchase.getTicketPurchaseOldId())
                                .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

                        ticketPurchaseOld.setStatus(ETicketPurchaseStatus.PURCHASED);
                        ticketPurchaseRepository.save(ticketPurchaseOld);

                        var zoneActivityOld = ticketPurchaseOld.getZoneActivity();
                        var seatActivityOld = ticketPurchaseOld.getSeatActivity();
                        if(zoneActivityOld != null && seatActivityOld != null) {
                            seatActivityOld.setStatus(ESeatActivityStatus.SOLD);
                            seatActivityRepository.save(seatActivityOld);
                        }
                    }
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
                    if(ticketPurchase.getTicketPurchaseOldId()!= null){
                        TicketPurchase ticketPurchaseOld = ticketPurchaseRepository
                                .findById(ticketPurchase.getTicketPurchaseOldId())
                                .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

                        ticketPurchaseOld.setStatus(ETicketPurchaseStatus.PURCHASED);
                        ticketPurchaseRepository.save(ticketPurchaseOld);

                        var ticketMappingOld = ticketMappingRepository.findTicketMappingByTicketIdAndEventActivityId(
                                ticketPurchaseOld.getTicket().getTicketId(), ticketPurchaseOld.getEventActivity().getEventActivityId())
                                .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                        if(ticketMappingOld.getQuantity() == 0) {
                            ticketMappingOld.setQuantity(ticketMappingOld.getQuantity());
                            ticketMappingOld.setStatus(true);
                            ticketMappingRepository.save(ticketMappingOld);
                        }else {
                            ticketMappingOld.setQuantity(ticketMappingOld.getQuantity() + ticketPurchaseOld.getQuantity());
                            ticketMappingRepository.save(ticketMappingOld);
                        }
                    }
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
        //chu y
        TicketPurchase ticketPurchase = ticketPurchaseRepository
                .findById(checkinLog.getOrder().getOrderId())
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
    public PaginationResponse<MyTicketResponse> getTicketPurchasesByAccount(int page, int size, String sortDirection) {
        appUtils.checkRole(ERole.BUYER, ERole.ORGANIZER);

        if (page < 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
        }

        int accountId = appUtils.getAccountFromAuthentication().getAccountId();

        List<MyTicketFlatDTO> flatDTOs = orderRepository.findAllTicketInfoForAccount(
                accountId, EOrderStatus.SUCCESSFUL, ETicketPurchaseStatus.PURCHASED
        );

        if (flatDTOs.isEmpty()) {
            return new PaginationResponse<>(Collections.emptyList(), page, 0, 0, size);
        }

        Map<Integer, MyTicketResponse> orderMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Duyệt qua danh sách flatDTOs để xây dựng MyTicketResponse
        for (MyTicketFlatDTO flat : flatDTOs) {
            MyTicketResponse response = orderMap.computeIfAbsent(flat.getOrderId(), oid -> {
                MyTicketResponse res = new MyTicketResponse();
                res.setOrderId(flat.getOrderId());
                res.setOrderCode(flat.getOrderCode());
                res.setOrderDate(flat.getOrderDate().format(formatter));
                res.setTotalPrice(flat.getTotalAmount());
                res.setTotalDiscount(flat.getTotalAmountDiscount());
                res.setEventId(flat.getEventId());
                res.setEventActivityId(flat.getEventActivityId());
                res.setEventCategoryId(flat.getEventCategoryId());
                res.setEventName(flat.getEventName());
                res.setEventDate(flat.getEventDate());
                res.setEventStartTime(flat.getEventStartTime());
                res.setTimeBuyOrder(flat.getOrderDate().format(formatter));
                res.setLocationName(flat.getLocationName());
                res.setLocation(Stream.of(flat.getAddress(), flat.getWard(), flat.getDistrict(), flat.getCity())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(", ")));
                res.setQrCode(flat.getQrCode());
                res.setLogo(flat.getLogo());
                res.setBanner(flat.getBanner());
                res.setTicketPurchases(new ArrayList<>());
                res.setIshaveSeatmap(Boolean.TRUE.equals(flat.getHasSeatMap()));
                res.setQuantityOrdered(0); // Khởi tạo số lượng vé đã mua
                return res;
            });

            TicketOwnerResponse dto = new TicketOwnerResponse();
            dto.setTicketPurchaseId(flat.getTicketPurchaseId());
            dto.setPrice(flat.getPrice());
            dto.setSeatCode(flat.getSeatCode());
            dto.setTicketType(flat.getTicketName());
            dto.setZoneName(flat.getZoneName());
            dto.setQuantity(flat.getQuantity());

            response.getTicketPurchases().add(dto);

            // Cộng dồn số lượng vé cho tất cả các ticketPurchase trong cùng 1 order
            response.setQuantityOrdered(response.getQuantityOrdered() + flat.getQuantity());
        }

        List<MyTicketResponse> responses = new ArrayList<>(orderMap.values());

        // Sort theo orderDate
        responses.sort((o1, o2) -> {
            if ("desc".equalsIgnoreCase(sortDirection)) {
                return o2.getOrderDate().compareTo(o1.getOrderDate());
            } else {
                return o1.getOrderDate().compareTo(o2.getOrderDate());
            }
        });

        int totalElements = responses.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        if (fromIndex >= totalElements) {
            return new PaginationResponse<>(Collections.emptyList(), page, totalPages, totalElements, size);
        }

        List<MyTicketResponse> pageItems = responses.subList(fromIndex, toIndex);

        return new PaginationResponse<>(pageItems, page, totalPages, totalElements, size);
    }



    @Override
    public PaginationResponse<MyTicketDTO> searchTicketPurchasesByEventName(int page, int size, String sortDirection, String eventName) {
        appUtils.checkRole(ERole.BUYER, ERole.ORGANIZER);

        if (!appUtils.getAccountFromAuthentication().getRole().getRoleName().equals(ERole.BUYER)
                && !appUtils.getAccountFromAuthentication().getRole().getRoleName().equals(ERole.ORGANIZER)) {
            throw new AppException(ErrorCode.NOT_PERMISSION);
        }

        List<TicketPurchase> ticketPurchases = ticketPurchaseRepository
                .getTicketPurchasesByAccount(appUtils.getAccountFromAuthentication().getAccountId());

        if (ticketPurchases.isEmpty()) {
            return new PaginationResponse<>(Collections.emptyList(), page, 0, 0, size);
        }

        List<MyTicketDTO> myTicketDTOS = ticketPurchases.stream()
                .filter(tp -> tp.getStatus().equals(ETicketPurchaseStatus.PURCHASED))
                .filter(tp -> tp.getEvent() != null && tp.getEvent().getEventName() != null
                        && tp.getEvent().getEventName().toLowerCase().contains(
                        eventName != null ? eventName.toLowerCase() : ""))
                .map(tp -> {
                    MyTicketDTO dto = new MyTicketDTO();

                    if (tp.getEvent() != null) {
                        OrderDetail orderDetail = orderDetailRepository.findOrderDetailByTicketPurchase_TicketPurchaseId(tp.getTicketPurchaseId())
                                .orElseThrow(() -> new AppException(ErrorCode.ORDER_DETAIL_NOT_FOUND));
                        Order order = orderRepository.findById(orderDetail.getOrder().getOrderId())
                                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

                        dto.setEventId(tp.getEvent().getEventId());
                        dto.setEventCategoryId(tp.getEvent().getCategory().getEventCategoryId());
                        dto.setEventName(tp.getEvent().getEventName());
                        dto.setLocationName(tp.getEvent().getLocationName());
                        dto.setLogo(tp.getEvent().getLogoURL());
                        dto.setBanner(tp.getEvent().getBannerURL());
                        dto.setIshaveSeatmap(tp.getEvent().getSeatMap() != null);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                        dto.setTimeBuyTicket(order.getOrderDate().format(formatter));                        StringBuilder locationBuilder = new StringBuilder();
                        if (tp.getEvent().getAddress() != null) locationBuilder.append(tp.getEvent().getAddress()).append(", ");
                        if (tp.getEvent().getWard() != null) locationBuilder.append(tp.getEvent().getWard()).append(", ");
                        if (tp.getEvent().getDistrict() != null) locationBuilder.append(tp.getEvent().getDistrict()).append(", ");
                        if (tp.getEvent().getCity() != null) locationBuilder.append(tp.getEvent().getCity());

                        if (locationBuilder.length() > 0 && locationBuilder.lastIndexOf(", ") == locationBuilder.length() - 2) {
                            locationBuilder.delete(locationBuilder.length() - 2, locationBuilder.length());
                        }

                        dto.setLocation(locationBuilder.length() > 0 ? locationBuilder.toString() : null);
                    }

                    if (tp.getEventActivity() != null) {
                        dto.setEventActivityId(tp.getEventActivity().getEventActivityId());
                        dto.setEventDate(tp.getEventActivity().getDateEvent());
                        dto.setEventStartTime(tp.getEventActivity().getStartTimeEvent());
                    }

                    if (tp.getTicket() != null) {
                        dto.setPrice(tp.getTicket().getPrice());
                        dto.setTicketType(tp.getTicket().getTicketName());
                    }

                    if (tp.getZoneActivity() != null && tp.getZoneActivity().getZone() != null) {
                        dto.setZoneName(tp.getZoneActivity().getZone().getZoneName());
                    }

                    if (tp.getSeatActivity() != null && tp.getSeatActivity().getSeat() != null) {
                        dto.setSeatCode(tp.getSeatActivity().getSeat().getSeatName());
                    }

                    dto.setQuantity(tp.getQuantity());
//                    dto.setQrCode(tp.getQrCode());
                    dto.setTicketPurchaseId(tp.getTicketPurchaseId());

                    return dto;
                }).collect(Collectors.toList());

        // Sort theo timeBuyTicket
        if (sortDirection != null && sortDirection.equalsIgnoreCase("desc")) {
            myTicketDTOS.sort(Comparator.comparing(MyTicketDTO::getTimeBuyTicket, Comparator.nullsLast(Comparator.reverseOrder())));
        } else {
            myTicketDTOS.sort(Comparator.comparing(MyTicketDTO::getTimeBuyTicket, Comparator.nullsLast(Comparator.naturalOrder())));
        }

        int totalElements = myTicketDTOS.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        if (fromIndex >= totalElements) {
            return new PaginationResponse<>(Collections.emptyList(), page, totalPages, totalElements, size);
        }

        List<MyTicketDTO> pageItems = myTicketDTOS.subList(fromIndex, toIndex);

        return new PaginationResponse<>(pageItems, page, totalPages, totalElements, size);
    }


    @Override
    public TicketQRResponse decryptQrCode(QrCodeRequest encryptedQrCode) {
        try {
            // 1. Kiểm tra đầu vào
            if (encryptedQrCode.getQrCode() == null || encryptedQrCode.getQrCode().trim().isEmpty()) {
                throw new AppException(ErrorCode.INVALID_QR_CODE);
            }
            String cleanedQrCode = encryptedQrCode.getQrCode().trim();

            // 2. Giải mã Base64 URL-safe
            byte[] encryptedBytes;
            try {
                encryptedBytes = Base64.getUrlDecoder().decode(cleanedQrCode);
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_QR_CODE);
            }

            // 3. Giải mã AES
            SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] compressedBytes;
            try {
                compressedBytes = cipher.doFinal(encryptedBytes);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                throw new AppException(ErrorCode.INVALID_QR_CODE);
            }

            // 4. Giải nén dữ liệu JSON
            byte[] decompressedBytes;
            try {
                decompressedBytes = decompress(compressedBytes);
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_QR_CODE);
            }
            String json = new String(decompressedBytes, StandardCharsets.UTF_8);

            // Debug JSON
            System.out.println("Decrypted JSON: " + json);

            // 5. Chuyển JSON thành DTO
            ObjectMapper mapper = new ObjectMapper();
            TicketQrCodeDTO dto;
            try {
                dto = mapper.readValue(json, TicketQrCodeDTO.class);
            } catch (JsonProcessingException e) {
                throw new AppException(ErrorCode.INVALID_QR_CODE);
            }

            // 6. Lấy thông tin sự kiện
            var eventActivity = eventActivityRepository
                    .findById(dto.getEvent_activity_id())
                    .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

            // 7. Kiểm tra thời gian sự kiện
            ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
            ZonedDateTime nowVN = ZonedDateTime.now(vietnamZone);

            LocalDateTime eventStartLocal = LocalDateTime.of(eventActivity.getDateEvent(), eventActivity.getStartTimeEvent());
            ZonedDateTime eventStartVN = eventStartLocal.atZone(vietnamZone);

            LocalDateTime eventEndLocal = LocalDateTime.of(eventActivity.getDateEvent(), eventActivity.getEndTimeEvent());
            ZonedDateTime eventEndVN = eventEndLocal.atZone(vietnamZone);

//            if (nowVN.isBefore(eventStartVN.minusHours(3))) {
//                throw new AppException(ErrorCode.EVENT_ACTIVITY_NOT_STARTED_YET);
//            }
//            if (nowVN.isAfter(eventEndVN)) {
//                throw new AppException(ErrorCode.EVENT_ACTIVITY_EXPIRED);
//            }

            // 8. Lấy order
            var order = orderRepository
                    .findById(dto.getOrder_id())
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

            // 9. Lấy checkinLog
            var checkinLog = checkinLogRepository.findById(dto.getCheckin_Log_id())
                    .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_LOG_NOT_FOUND));

            // 10. Lấy người dùng (consumer)
            var consumer = accountRepository.findAccountByUserName(dto.getUser_name())
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

            var event = eventActivity.getEvent();

            // 11. Cập nhật trạng thái check-in (bỏ comment nếu cần)
        /*
        checkinLog.setCheckinStatus(ECheckinLogStatus.CHECKED_IN);
        checkinLog.setCheckinTime(nowVN.toLocalDateTime());
        checkinLog.setCheckinLocation(event.getLocationName());
        checkinLog.setStaff(appUtils.getAccountFromAuthentication());
        checkinLogRepository.save(checkinLog);
        */

            // 12. Tạo response
            TicketQRResponse response = new TicketQRResponse();
            response.setEventName(event.getEventName());
            response.setEventDate(eventActivity.getDateEvent());
            response.setStartTime(eventActivity.getStartTimeEvent());
            response.setOrderCode(order.getOrderCode());
            response.setUserName(dto.getUser_name());
            response.setFullName(consumer.getFirstName() + " " + consumer.getLastName());
            response.setCheckinStatus(checkinLog.getCheckinStatus().name());
            response.setOrderCode(order.getOrderCode());
            List<OrderDetail> orderDetails = (List<OrderDetail>) order.getOrderDetails();
            List<TicketQRResponse.TicketDetailResponse> ticketDetails = new ArrayList<>();

            for (OrderDetail orderDetail : orderDetails) {
                TicketPurchase ticketPurchase = orderDetail.getTicketPurchase();
                if (ticketPurchase != null) {
                    TicketQRResponse.TicketDetailResponse detail = new TicketQRResponse.TicketDetailResponse();
                    detail.setTicketPurchaseId(ticketPurchase.getTicketPurchaseId());
                    detail.setPrice(ticketPurchase.getTicket().getPrice());
                    detail.setSeatName(AppUtils.convertSeatName(ticketPurchase.getSeatActivity().getSeat().getSeatName()));
                    detail.setTicketType(ticketPurchase.getTicket().getTicketName());
                    detail.setZoneName(ticketPurchase.getZoneActivity().getZone().getZoneName());
                    detail.setQuantity(ticketPurchase.getQuantity());
                    ticketDetails.add(detail);
                }
            }

            response.setTicketDetails(ticketDetails);

            return response;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_QR_CODE);
        }
    }
    public static byte[] decompress(byte[] compressedBytes) throws Exception {
        Inflater inflater = new Inflater();
        inflater.setInput(compressedBytes);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
        } catch (DataFormatException e) {
            throw new IllegalArgumentException("Lỗi giải nén dữ liệu: " + e.getMessage(), e);
        } finally {
            outputStream.close();
            inflater.end();
        }

        return outputStream.toByteArray();
    }


    @Override
    public int countTicketPurchaseStatusByPurchased() {
        var count = ticketPurchaseRepository.countTicketPurchasesByStatus(ETicketPurchaseStatus.PURCHASED);
        return count;
    }
}
