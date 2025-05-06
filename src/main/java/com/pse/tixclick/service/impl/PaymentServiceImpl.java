package com.pse.tixclick.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.PaymentDTO;
import com.pse.tixclick.payload.dto.TicketQrCodeDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.CheckinLog;
import com.pse.tixclick.payload.entity.entity_enum.*;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.payment.*;
import com.pse.tixclick.payload.entity.seatmap.*;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketMapping;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;
import com.pse.tixclick.payload.response.PayOSResponse;
import com.pse.tixclick.payload.response.PaymentResponse;
import com.pse.tixclick.payment.PayOSUtils;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.PaymentService;
import com.pse.tixclick.utils.AppUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PayOSUtils payOSUtils;

    @Autowired
    ModelMapper mapper;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderDetailRepository orderDetailRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TicketPurchaseRepository ticketPurchaseRepository;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    ZoneRepository zoneRepository;

    @Autowired
    CheckinLogRepository checkinLogRepository;

    @Autowired
    EventActivityRepository eventActivityRepository;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    ZoneActivityRepository zoneActivityRepository;

    @Autowired
    SeatActivityRepository seatActivityRepository;

    @Autowired
    TicketMappingRepository ticketMappingRepository;

    @Autowired
    AppUtils appUtils;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    OrderServiceImpl orderServiceImpl;

    @Override
    public PayOSResponse changeOrderStatusPayOs(int orderId) {
        return null;
    }

    @Override
    public PayOSResponse createPaymentLink(int orderId, String voucherCode, long expiredTime, HttpServletRequest request) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        Account account = accountRepository
                .findById(order.getAccount().getAccountId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        List<Payment> paymentHistory = paymentRepository.findByOrderId(order.getOrderId());
        if (!paymentHistory.isEmpty() && paymentHistory.stream().anyMatch(payment -> payment.getStatus().equals(EPaymentStatus.SUCCESSFULLY))) {
            throw new RuntimeException("Payment is already completed");
        }

        int totalAmount = (int) Math.round(order.getTotalAmountDiscount());
        long orderCode = Long.parseLong(order.getOrderCode());
        String baseUrl = getBaseUrl(request);

        ItemData itemData = ItemData.builder()
                .name(order.getOrderCode())
                .quantity(1)
                .price(totalAmount)
                .build();

        // Xây dựng returnUrl và cancelUrl
        String returnUrl = baseUrl + "/payment/queue" +
                "?orderId=" + order.getOrderId() +
                "&voucherCode=" + voucherCode +
                "&userName=" + account.getUserName() +
                "&amount=" + itemData.getPrice() +
                "&name=" + itemData.getName();

        String cancelUrl = baseUrl + "/payment/queue"  +
                "?orderId=" + order.getOrderId() +
                "&voucherCode=" + voucherCode +
                "&userName=" + account.getUserName() +
                "&amount=" + itemData.getPrice() +
                "&name=" + itemData.getName();
        long expiredAt = Instant.now().getEpochSecond() + expiredTime;
        PaymentData paymentData = PaymentData.builder()
                .expiredAt(expiredAt)
                .orderCode(orderCode)
                .amount(totalAmount)
                .description("Thanh toán đơn hàng")
                .returnUrl(returnUrl)  // Sử dụng returnUrl đã xây dựng
                .cancelUrl(cancelUrl)  // Sử dụng cancelUrl đã xây dựng
                .item(itemData)
                .build();

        CheckoutResponseData result = payOSUtils.payOS().createPaymentLink(paymentData);

        Payment payment = new Payment();
        payment.setAmount(totalAmount);
        payment.setStatus(EPaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setOrderCode(order.getOrderCode());
        payment.setOrder(order);
        payment.setPaymentMethod("Thanh toán ngân hàng");
        payment.setAccount(account);
        paymentRepository.save(payment);

        // Trả về kết quả
        return PayOSResponse.builder()
                .error("ok")
                .message("success")
                .data(objectMapper.valueToTree(result))
                .build();
    }

    @Override
    public PayOSResponse changTicket(int purchaseId, List<CreateTicketPurchaseRequest> ticketChange, String caseTicket, HttpServletRequest request) throws Exception {
        var context = SecurityContextHolder.getContext();
        var userName = context.getAuthentication().getName();

        var account = accountRepository.findAccountByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        var ticketPurchase = ticketPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

        if (ticketPurchase.getStatus().equals(ETicketPurchaseStatus.PENDING)) {
            throw new AppException(ErrorCode.TICKET_PURCHASE_DONT_BUY);
        }

        if (ticketPurchase.getStatus().equals(ETicketPurchaseStatus.CANCELLED)) {
            throw new AppException(ErrorCode.TICKET_PURCHASE_CANCELLED);
        }

        double totalAmount = 0;
        Ticket oldTicket = ticketPurchase.getTicket();
        List<TicketPurchase> newPurchases = new ArrayList<>();
        String orderNote = "";

        switch (caseTicket) {
            case "noSeatMap" -> {
                orderNote = "Đổi vé nhiều loại";

                int totalRequestedQuantity = ticketChange.stream()
                        .mapToInt(CreateTicketPurchaseRequest::getQuantity)
                        .sum();

                int originalQuantity = ticketPurchase.getQuantity();

                if (totalRequestedQuantity > originalQuantity) {
                    throw new AppException(ErrorCode.TICKET_REQUEST_EXCEED_QUANTITY);
                }

                for (CreateTicketPurchaseRequest ticketReq : ticketChange) {
                    Ticket newTicket = ticketRepository.findById(ticketReq.getTicketId())
                            .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

                    TicketMapping ticketMapping = ticketMappingRepository
                            .findTicketMappingByTicketIdAndEventActivityId(
                                    newTicket.getTicketId(),
                                    ticketPurchase.getEventActivity().getEventActivityId())
                            .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                    if (ticketMapping.getQuantity() < ticketReq.getQuantity()) {
                        throw new AppException(ErrorCode.TICKET_NOT_ENOUGH);
                    }

                    ticketMapping.setQuantity(ticketMapping.getQuantity() - ticketReq.getQuantity());
                    ticketMappingRepository.save(ticketMapping);

                    double diff = newTicket.getPrice() - oldTicket.getPrice();
                    totalAmount += Math.max(diff, 0) * ticketReq.getQuantity();

                    TicketPurchase newPurchase = new TicketPurchase();
                    newPurchase.setAccount(account);
                    newPurchase.setEventActivity(ticketPurchase.getEventActivity());
                    newPurchase.setEvent(ticketPurchase.getEvent());
                    newPurchase.setQuantity(ticketReq.getQuantity());
                    newPurchase.setTicket(newTicket);
                    newPurchase.setZoneActivity(null);
                    newPurchase.setSeatActivity(null);
                    newPurchase.setStatus(ETicketPurchaseStatus.PENDING); // status cho vé đổi
                    newPurchase.setTicketPurchaseOldId(ticketPurchase.getTicketPurchaseId());
                    ticketPurchaseRepository.save(newPurchase);
                    newPurchases.add(newPurchase);
                }

                int remainingQuantity = originalQuantity - totalRequestedQuantity;

                if (remainingQuantity > 0) {
                    TicketPurchase keepPurchase = new TicketPurchase();
                    keepPurchase.setAccount(account);
                    keepPurchase.setEventActivity(ticketPurchase.getEventActivity());
                    keepPurchase.setEvent(ticketPurchase.getEvent());
                    keepPurchase.setQuantity(remainingQuantity);
                    keepPurchase.setTicket(oldTicket); // giữ nguyên vé cũ
                    keepPurchase.setZoneActivity(null);
                    keepPurchase.setSeatActivity(null);
                    keepPurchase.setStatus(ETicketPurchaseStatus.PENDING);
                    keepPurchase.setTicketPurchaseOldId(ticketPurchase.getTicketPurchaseId());
                    ticketPurchaseRepository.save(keepPurchase);
                    newPurchases.add(keepPurchase);
                }

                if (totalRequestedQuantity > 0) {
                    TicketMapping oldMapping = ticketMappingRepository
                            .findTicketMappingByTicketIdAndEventActivityId(
                                    oldTicket.getTicketId(),
                                    ticketPurchase.getEventActivity().getEventActivityId())
                            .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                    oldMapping.setQuantity(oldMapping.getQuantity() + totalRequestedQuantity);
                    ticketMappingRepository.save(oldMapping);
                }
            }

            case "changeSeat" -> {
                orderNote = "Đổi ghế";
                CreateTicketPurchaseRequest ticket = ticketChange.get(0);
                Ticket newTicket = ticketRepository
                        .findById(ticket.getTicketId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

                totalAmount = Math.max(newTicket.getPrice() - ticketPurchase.getTicket().getPrice(), 0);

                SeatActivity oldSeatActivity = ticketPurchase.getSeatActivity();
                if (oldSeatActivity != null) {
                    oldSeatActivity.setStatus(ESeatActivityStatus.AVAILABLE);
                    seatActivityRepository.save(oldSeatActivity);
                }

                if (ticket.getSeatId() == 0) {
                    ZoneActivity newZoneActivity = zoneActivityRepository
                            .findById(ticket.getZoneId())
                            .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                    TicketPurchase newPurchase = new TicketPurchase();
                    newPurchase.setAccount(account);
                    newPurchase.setEventActivity(ticketPurchase.getEventActivity());
                    newPurchase.setEvent(ticketPurchase.getEvent());
                    newPurchase.setQuantity(1);
                    newPurchase.setTicket(newTicket);
                    newPurchase.setZoneActivity(newZoneActivity);
                    newPurchase.setSeatActivity(null);
                    newPurchase.setStatus(ETicketPurchaseStatus.PENDING);
                    newPurchase.setTicketPurchaseOldId(ticketPurchase.getTicketPurchaseId());
                    ticketPurchaseRepository.save(newPurchase);
                    newPurchases.add(newPurchase);
                } else {
                    int eventActivityId = ticketPurchase.getEventActivity().getEventActivityId();
                    SeatActivity newSeatActivity = seatActivityRepository
                            .findByEventActivityIdAndSeatId(eventActivityId, ticket.getSeatId())
                            .orElseThrow(() -> new AppException(ErrorCode.SEAT_ACTIVITY_NOT_FOUND));

                    if (newSeatActivity.getStatus() != ESeatActivityStatus.AVAILABLE) {
                        throw new AppException(ErrorCode.SEAT_ALREADY_BOOKED);
                    }

                    TicketPurchase newPurchase = new TicketPurchase();
                    newPurchase.setAccount(account);
                    newPurchase.setEventActivity(ticketPurchase.getEventActivity());
                    newPurchase.setEvent(ticketPurchase.getEvent());
                    newPurchase.setQuantity(1);
                    newPurchase.setTicket(newTicket);
                    newPurchase.setSeatActivity(newSeatActivity);
                    newPurchase.setZoneActivity(newSeatActivity.getZoneActivity());
                    newPurchase.setStatus(ETicketPurchaseStatus.PENDING);
                    newPurchase.setTicketPurchaseOldId(ticketPurchase.getTicketPurchaseId());
                    ticketPurchaseRepository.save(newPurchase);
                    newPurchases.add(newPurchase);

                    newSeatActivity.setStatus(ESeatActivityStatus.SOLD);
                    seatActivityRepository.save(newSeatActivity);
                }
            }

            case "changeZone" -> {
                orderNote = "Đổi zone";

                int totalRequestedQuantity = ticketChange.stream()
                        .mapToInt(CreateTicketPurchaseRequest::getQuantity)
                        .sum();

                int originalQuantity = ticketPurchase.getQuantity();

                if (totalRequestedQuantity > originalQuantity) {
                    throw new AppException(ErrorCode.TICKET_REQUEST_EXCEED_QUANTITY);
                }

                // Hủy ghế cũ nếu có
                if (ticketPurchase.getSeatActivity() != null) {
                    SeatActivity oldSeatActivity = ticketPurchase.getSeatActivity();
                    oldSeatActivity.setStatus(ESeatActivityStatus.AVAILABLE);
                    seatActivityRepository.save(oldSeatActivity);
                }

                for (CreateTicketPurchaseRequest ticketReq : ticketChange) {
                    Ticket newTicket = ticketRepository.findById(ticketReq.getTicketId())
                            .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

                    ZoneActivity newZoneActivity = zoneActivityRepository
                            .findByEventActivityIdAndZoneId(ticketReq.getEventActivityId(),ticketReq.getZoneId())
                            .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                    double diff = newTicket.getPrice() - oldTicket.getPrice();
                    totalAmount += Math.max(diff, 0) * ticketReq.getQuantity();

                    if (ticketReq.getSeatId() != 0) {
                        for (int i = 0; i < ticketReq.getQuantity(); i++) {
                            SeatActivity newSeatActivity = seatActivityRepository
                                    .findByEventActivityIdAndSeatId(ticketPurchase.getEventActivity().getEventActivityId(), ticketReq.getSeatId())
                                    .orElseThrow(() -> new AppException(ErrorCode.SEAT_ACTIVITY_NOT_FOUND));

                            if (newSeatActivity.getStatus() != ESeatActivityStatus.AVAILABLE) {
                                throw new AppException(ErrorCode.SEAT_ALREADY_BOOKED);
                            }

                            TicketPurchase newPurchase = new TicketPurchase();
                            newPurchase.setAccount(account);
                            newPurchase.setEventActivity(ticketPurchase.getEventActivity());
                            newPurchase.setEvent(ticketPurchase.getEvent());
                            newPurchase.setQuantity(1);
                            newPurchase.setTicket(newTicket);
                            newPurchase.setZoneActivity(newZoneActivity);
                            newPurchase.setSeatActivity(newSeatActivity);
                            newPurchase.setStatus(ETicketPurchaseStatus.PENDING); // CHỈ ĐỔI => PENDING
                            newPurchase.setTicketPurchaseOldId(ticketPurchase.getTicketPurchaseId());
                            ticketPurchaseRepository.save(newPurchase);
                            newPurchases.add(newPurchase);

                            newSeatActivity.setStatus(ESeatActivityStatus.SOLD);
                            seatActivityRepository.save(newSeatActivity);
                        }
                    } else {
                        TicketPurchase newPurchase = new TicketPurchase();
                        newPurchase.setAccount(account);
                        newPurchase.setEventActivity(ticketPurchase.getEventActivity());
                        newPurchase.setEvent(ticketPurchase.getEvent());
                        newPurchase.setQuantity(ticketReq.getQuantity());
                        newPurchase.setTicket(newTicket);
                        newPurchase.setZoneActivity(newZoneActivity);
                        newPurchase.setSeatActivity(null);
                        newPurchase.setStatus(ETicketPurchaseStatus.PENDING); // CHỈ ĐỔI => PENDING
                        newPurchase.setTicketPurchaseOldId(ticketPurchase.getTicketPurchaseId());
                        ticketPurchaseRepository.save(newPurchase);
                        newPurchases.add(newPurchase);
                    }
                }

                int remainingQuantity = originalQuantity - totalRequestedQuantity;

                if (remainingQuantity > 0) {
                    // Tạo vé giữ lại nếu còn vé cũ chưa đổi
                    TicketPurchase keepOld = new TicketPurchase();
                    keepOld.setAccount(account);
                    keepOld.setEventActivity(ticketPurchase.getEventActivity());
                    keepOld.setEvent(ticketPurchase.getEvent());
                    keepOld.setQuantity(remainingQuantity);
                    keepOld.setTicket(oldTicket);
                    keepOld.setZoneActivity(ticketPurchase.getZoneActivity()); // giữ nguyên zone cũ
                    keepOld.setSeatActivity(null);
                    keepOld.setStatus(ETicketPurchaseStatus.PENDING);
                    keepOld.setTicketPurchaseOldId(ticketPurchase.getTicketPurchaseId());
                    ticketPurchaseRepository.save(keepOld);
                    newPurchases.add(keepOld);
                }

                // Nếu tất cả vé cũ đã đổi, hủy luôn vé cũ ban đầu
                if (totalRequestedQuantity == originalQuantity) {
                    ticketPurchase.setStatus(ETicketPurchaseStatus.CANCELLED);
                    ticketPurchaseRepository.save(ticketPurchase);
                }
            }

        }

        ticketPurchase.setStatus(ETicketPurchaseStatus.CANCELLED);
        ticketPurchaseRepository.save(ticketPurchase);

        // 🔁 Làm tròn nếu nhỏ hơn 10,000
        if (totalAmount > 0 && totalAmount < 10000) {
            totalAmount = 10000;
        }

        if (totalAmount == 0) {
            Order order = new Order();
            order.setOrderCode(orderServiceImpl.orderCodeAutomationCreating());
            order.setStatus(EOrderStatus.SUCCESSFUL);
            order.setTotalAmount(0);
            order.setTotalAmountDiscount(0);
            order.setNote(orderNote);
            order.setOrderDate(LocalDateTime.now());
            order.setAccount(account);
            orderRepository.save(order);

            for (TicketPurchase newPurchase : newPurchases) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrder(order);
                orderDetail.setTicketPurchase(newPurchase);
                orderDetail.setAmount(newPurchase.getTicket().getPrice() * newPurchase.getQuantity());
                orderDetailRepository.save(orderDetail);

                StringBuilder locationBuilder = new StringBuilder();

                if(newPurchase.getEvent().getAddress() != null && !newPurchase.getEvent().getAddress().trim().isEmpty()) {
                    locationBuilder.append(newPurchase.getEvent().getAddress()).append(", ");
                }

                if (newPurchase.getEvent().getWard() != null && !newPurchase.getEvent().getWard().trim().isEmpty()) {
                    locationBuilder.append(newPurchase.getEvent().getWard()).append(", ");
                }

                if (newPurchase.getEvent().getDistrict() != null && !newPurchase.getEvent().getDistrict().trim().isEmpty()) {
                    locationBuilder.append(newPurchase.getEvent().getDistrict()).append(", ");
                }

                if (newPurchase.getEvent().getCity() != null && !newPurchase.getEvent().getCity().trim().isEmpty()) {
                    locationBuilder.append(newPurchase.getEvent().getCity());
                }

                if (locationBuilder.length() > 0 && locationBuilder.lastIndexOf(", ") == locationBuilder.length() - 2) {
                    locationBuilder.delete(locationBuilder.length() - 2, locationBuilder.length());
                }

                CheckinLog checkinLog = new CheckinLog();
                checkinLog.setCheckinTime(null);
                checkinLog.setCheckinDevice("Mobile");
                checkinLog.setTicketPurchase(newPurchase);
                checkinLog.setAccount(account);
                checkinLog.setCheckinStatus(ECheckinLogStatus.PENDING);
                checkinLog.setStaff(null);
                checkinLog.setCheckinLocation(locationBuilder.toString());
                checkinLogRepository.save(checkinLog);






                TicketQrCodeDTO qrcode = new TicketQrCodeDTO();
                qrcode.setTicket_name(newPurchase.getTicket().getTicketName());
                qrcode.setPurchase_date(new Date());
                qrcode.setEvent_name(newPurchase.getEvent().getEventName());
                qrcode.setActivity_name(newPurchase.getEventActivity().getActivityName());
                qrcode.setZone_name(newPurchase.getZoneActivity() != null ? newPurchase.getZoneActivity().getZone().getZoneName() : null);
                qrcode.setSeat_code(newPurchase.getSeatActivity() != null ? newPurchase.getSeatActivity().getSeat().getRowNumber() + newPurchase.getSeatActivity().getSeat().getSeatName() : null);
                qrcode.setSeat_row_number(newPurchase.getSeatActivity() != null ? newPurchase.getSeatActivity().getSeat().getRowNumber() : null);
                qrcode.setSeat_column_number(newPurchase.getSeatActivity() != null ? newPurchase.getSeatActivity().getSeat().getColumnNumber() : null);
                qrcode.setAccount_name(account.getUserName());
                qrcode.setEmail(account.getEmail());
                qrcode.setPhone(account.getPhone());
                qrcode.setCheckin_Log_id(0);
                qrcode.setStatus("Đã thanh toán");
                String qrCode1 = generateQRCode(qrcode);


                newPurchase.setStatus(ETicketPurchaseStatus.PENDING);
                newPurchase.setQrCode(qrCode1);
                ticketPurchaseRepository.save(newPurchase);
            }

            return PayOSResponse.builder()
                    .error("ok")
                    .message("Đổi vé thành công, không cần thanh toán")
                    .data(null)
                    .build();
        }

        Order order = new Order();
        order.setOrderCode(orderServiceImpl.orderCodeAutomationCreating());
        order.setStatus(EOrderStatus.PENDING);
        order.setTotalAmount(totalAmount);
        order.setTotalAmountDiscount(totalAmount);
        order.setNote(orderNote);
        order.setOrderDate(LocalDateTime.now());
        order.setAccount(account);
        orderRepository.save(order);

        for (TicketPurchase newPurchase : newPurchases) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setTicketPurchase(newPurchase);
            orderDetail.setAmount(newPurchase.getTicket().getPrice() * newPurchase.getQuantity());
            orderDetailRepository.save(orderDetail);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<Payment> paymentHistory = paymentRepository.findByOrderId(order.getOrderId());
        if (!paymentHistory.isEmpty() && paymentHistory.stream().anyMatch(payment -> payment.getStatus().equals(EPaymentStatus.SUCCESSFULLY))) {
            throw new RuntimeException("Thanh toán đã hoàn tất");
        }

        int totalAmountInt = (int) Math.round(totalAmount);
        long orderCode = Long.parseLong(order.getOrderCode());
        String baseUrl = getBaseUrl(request);

        ItemData itemData = ItemData.builder()
                .name(order.getOrderCode())
                .quantity(1)
                .price(totalAmountInt)
                .build();

        String returnUrl = baseUrl + "/payment/queue" +
                "?orderId=" + order.getOrderId() +
                "&voucherCode=" + "" +
                "&userName=" + account.getUserName() +
                "&amount=" + itemData.getPrice() +
                "&name=" + itemData.getName();

        String cancelUrl = returnUrl;

        long expiredAt = Instant.now().getEpochSecond() + 900L;
        PaymentData paymentData = PaymentData.builder()
                .expiredAt(expiredAt)
                .orderCode(orderCode)
                .amount(totalAmountInt)
                .description("Thanh toán đơn hàng")
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(itemData)
                .build();

        CheckoutResponseData result = payOSUtils.payOS().createPaymentLink(paymentData);

        Payment payment = new Payment();
        payment.setAmount(totalAmountInt);
        payment.setStatus(EPaymentStatus.PENDING);
        payment.setOrderCode(order.getOrderCode());
        payment.setOrder(order);
        payment.setPaymentMethod("Thanh toán ngân hàng");
        payment.setAccount(account);
        paymentRepository.save(payment);

        return PayOSResponse.builder()
                .error("ok")
                .message("Thành công")
                .data(objectMapper.valueToTree(result))
                .build();
    }

    @Override
    public PaymentResponse handleCallbackPayOS(HttpServletRequest request) throws Exception {
        String status = request.getParameter("status");
        String code = request.getParameter("code");
        String orderId = request.getParameter("orderId");
        String transactionNo = request.getParameter("id");
        String userName = request.getParameter("userName");
        String orderCode = request.getParameter("orderCode");
        String amount = request.getParameter("amount");
        String voucherCode = request.getParameter("voucherCode");

        Payment payment = paymentRepository.findPaymentByOrderCode(orderCode);

        if (code.equals("00") && status.equals("PAID")) {
            payment.setStatus(EPaymentStatus.SUCCESSFULLY);
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(payment);

            Order order = orderRepository
                    .findById(Integer.valueOf(orderId))
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

            order.setStatus(EOrderStatus.SUCCESSFUL);
            orderRepository.save(order);

            Account account = accountRepository
                    .findAccountByUserName(userName)
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

            List<OrderDetail> orderDetail = orderDetailRepository.findByOrderId(order.getOrderId());

            for(OrderDetail detail : orderDetail) {
                TicketPurchase ticketPurchase = ticketPurchaseRepository
                        .findById(detail.getTicketPurchase().getTicketPurchaseId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));
                //Kiểm tra trạng thái của ticketPurchase xem thanh toán hay huỷ nếu không thì đi xuống dưới
                if (ticketPurchase.getStatus().equals(ETicketPurchaseStatus.PENDING) ||
                        ticketPurchase.getStatus().equals(ETicketPurchaseStatus.PURCHASED)) {
                    //Nếu không ghế và có zone
                    if(ticketPurchase.getSeatActivity() == null && ticketPurchase.getZoneActivity() != null){
                        //kiểm tra Zone
                        ZoneActivity zoneActivity = zoneActivityRepository
                                .findByEventActivityIdAndZoneId(ticketPurchase.getZoneActivity().getEventActivity().getEventActivityId(), ticketPurchase.getZoneActivity().getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                        Zone zone = zoneRepository
                                .findById(ticketPurchase.getZoneActivity().getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                        List<ZoneActivity> zoneActivities = zoneActivityRepository
                                .findByZoneId(ticketPurchase.getZoneActivity().getZone().getZoneId());
                        boolean allZoneUnavailable = zoneActivities.stream()
                                .allMatch(zoneActivity1 -> zoneActivity.getAvailableQuantity() == 0);
                        if (allZoneUnavailable) {
                            zone.setStatus(false);
                        }

                        zoneRepository.save(zone);
                        zoneActivityRepository.save(zoneActivity);
                    }

                    //Nếu có ghế và có zone
                    if(ticketPurchase.getZoneActivity() != null && ticketPurchase.getSeatActivity() != null){
                        //kiểm tra Zone
                        ZoneActivity zoneActivity = zoneActivityRepository
                                .findByEventActivityIdAndZoneId(ticketPurchase.getZoneActivity().getEventActivity().getEventActivityId(), ticketPurchase.getZoneActivity().getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                        Zone zone = zoneRepository
                                .findById(ticketPurchase.getZoneActivity().getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                        List<ZoneActivity> zoneActivities = zoneActivityRepository
                                .findByZoneId(ticketPurchase.getZoneActivity().getZone().getZoneId());
                        boolean allZoneUnavailable = zoneActivities.stream()
                                .allMatch(zoneActivity1 -> zoneActivity.getAvailableQuantity() == 0);
                        if (allZoneUnavailable) {
                            zone.setStatus(false);
                        }

                        //Kiểm tra Seat
                        SeatActivity seatActivity = seatActivityRepository
                                .findByEventActivityIdAndSeatId(ticketPurchase.getSeatActivity().getEventActivity().getEventActivityId(), ticketPurchase.getSeatActivity().getSeat().getSeatId())
                                .orElseThrow(() -> new AppException(ErrorCode.SEAT_ACTIVITY_NOT_FOUND));

                        Seat seat = seatRepository
                                .findById(ticketPurchase.getSeatActivity().getSeat().getSeatId())
                                .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));

                        seatActivity.setStatus(ESeatActivityStatus.SOLD);
                        seatActivityRepository.save(seatActivity);

                        List<SeatActivity> seatActivities = seatActivityRepository.findBySeatId(ticketPurchase.getSeatActivity().getSeat().getSeatId());
                        boolean allSeatUnavailable = seatActivities.stream()
                                .allMatch(seatActivity1 -> seatActivity.getStatus().equals(ESeatActivityStatus.SOLD));
                        if (allSeatUnavailable) {
                            seat.setStatus(false);
                        }

                        seatRepository.save(seat);
                        zoneRepository.save(zone);
                    }
                }

                EventActivity eventActivity = eventActivityRepository
                        .findById(ticketPurchase.getEventActivity().getEventActivityId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

                Ticket ticket = ticketRepository
                        .findById(ticketPurchase.getTicket().getTicketId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

                Event event = eventRepository
                        .findById(ticketPurchase.getEvent().getEventId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

                CheckinLog checkinLog = new CheckinLog();
                checkinLog.setCheckinTime(null);
                checkinLog.setCheckinDevice("Mobile");
                checkinLog.setTicketPurchase(ticketPurchase);
                checkinLog.setAccount(account);


                StringBuilder locationBuilder = new StringBuilder();

                if(event.getAddress() != null && !event.getAddress().trim().isEmpty()) {
                    locationBuilder.append(event.getAddress()).append(", ");
                }

                if (event.getWard() != null && !event.getWard().trim().isEmpty()) {
                    locationBuilder.append(event.getWard()).append(", ");
                }

                if (event.getDistrict() != null && !event.getDistrict().trim().isEmpty()) {
                    locationBuilder.append(event.getDistrict()).append(", ");
                }

                if (event.getCity() != null && !event.getCity().trim().isEmpty()) {
                    locationBuilder.append(event.getCity());
                }

                if (locationBuilder.length() > 0 && locationBuilder.lastIndexOf(", ") == locationBuilder.length() - 2) {
                    locationBuilder.delete(locationBuilder.length() - 2, locationBuilder.length());
                }

                checkinLog.setCheckinLocation(locationBuilder.toString());
                checkinLog.setCheckinStatus(ECheckinLogStatus.PENDING);
                checkinLogRepository.save(checkinLog);

                TicketQrCodeDTO ticketQrCodeDTO = new TicketQrCodeDTO();
                String seatCode = null;
                if(ticketPurchase.getSeatActivity() != null) {
                    String input = ticketPurchase.getSeatActivity().getSeat().getRowNumber() + ticketPurchase.getSeatActivity().getSeat().getSeatName();
                    String result = convert(input);
                    seatCode = ticketPurchase.getZoneActivity().getZone().getZoneName() + "-" + result;

                    ticketQrCodeDTO.setTicket_name(ticket.getTicketName());
                    ticketQrCodeDTO.setPurchase_date(new Date());
                    ticketQrCodeDTO.setEvent_name(event.getEventName());
                    ticketQrCodeDTO.setActivity_name(eventActivity.getActivityName());
                    ticketQrCodeDTO.setZone_name(ticketPurchase.getZoneActivity().getZone().getZoneName());
                    ticketQrCodeDTO.setSeat_code(seatCode);
                    ticketQrCodeDTO.setSeat_row_number(ticketPurchase.getSeatActivity().getSeat().getRowNumber());
                    ticketQrCodeDTO.setSeat_column_number(ticketPurchase.getSeatActivity().getSeat().getColumnNumber());
                    ticketQrCodeDTO.setAccount_name(account.getUserName());
                    ticketQrCodeDTO.setEmail(account.getEmail());
                    ticketQrCodeDTO.setPhone(account.getPhone());
                    ticketQrCodeDTO.setCheckin_Log_id(checkinLog.getCheckinId());
                }
                else if(ticketPurchase.getSeatActivity() == null && ticketPurchase.getZoneActivity() != null) {
                    seatCode = null;
                    ticketQrCodeDTO.setTicket_name(ticket.getTicketName());
                    ticketQrCodeDTO.setPurchase_date(new Date());
                    ticketQrCodeDTO.setEvent_name(event.getEventName());
                    ticketQrCodeDTO.setActivity_name(eventActivity.getActivityName());
                    ticketQrCodeDTO.setZone_name(ticketPurchase.getZoneActivity().getZone().getZoneName());
                    ticketQrCodeDTO.setSeat_code(seatCode);
                    ticketQrCodeDTO.setSeat_row_number(null);
                    ticketQrCodeDTO.setSeat_column_number(null);
                    ticketQrCodeDTO.setAccount_name(account.getUserName());
                    ticketQrCodeDTO.setEmail(account.getEmail());
                    ticketQrCodeDTO.setPhone(account.getPhone());
                    ticketQrCodeDTO.setCheckin_Log_id(checkinLog.getCheckinId());
                }
                else if(ticketPurchase.getSeatActivity() == null && ticketPurchase.getZoneActivity() == null) {
                    seatCode = null;
                    ticketQrCodeDTO.setTicket_name(ticket.getTicketName());
                    ticketQrCodeDTO.setPurchase_date(new Date());
                    ticketQrCodeDTO.setEvent_name(event.getEventName());
                    ticketQrCodeDTO.setActivity_name(eventActivity.getActivityName());
                    ticketQrCodeDTO.setZone_name(null);
                    ticketQrCodeDTO.setSeat_code(seatCode);
                    ticketQrCodeDTO.setSeat_row_number(null);
                    ticketQrCodeDTO.setSeat_column_number(null);
                    ticketQrCodeDTO.setAccount_name(account.getUserName());
                    ticketQrCodeDTO.setEmail(account.getEmail());
                    ticketQrCodeDTO.setPhone(account.getPhone());
                    ticketQrCodeDTO.setCheckin_Log_id(checkinLog.getCheckinId());
                }

                String qrCode = generateQRCode(ticketQrCodeDTO);
                ticketPurchase.setQrCode(qrCode);
                ticketPurchase.setStatus(ETicketPurchaseStatus.PURCHASED);
                ticketPurchaseRepository.save(ticketPurchase);
            }

            Transaction transactionExisted = transactionRepository
                    .findByTransactionCode(transactionNo);

            if(transactionExisted != null){
                throw new AppException(ErrorCode.TRANSACTION_EXISTED);
            }


            Transaction transaction = new Transaction();
            transaction.setAmount(Double.valueOf(amount));
            transaction.setTransactionCode(transactionNo);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setDescription("Thanh toan don hang: " + orderCode);
            transaction.setAccount(account);
            transaction.setPayment(payment);
            transaction.setStatus(ETransactionStatus.SUCCESS);
            transaction.setContractPayment(null);
            transaction.setType(ETransactionType.DIRECT_PAYMENT);
            transactionRepository.save(transaction);

            return new PaymentResponse(status, "SUCCESSFUL", mapper.map(payment, PaymentResponse.class));
        }
        else {
            payment.setStatus(EPaymentStatus.FAILURE);
            paymentRepository.save(payment);

            Order order = orderRepository
                    .findById(Integer.valueOf(orderId))
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

            order.setStatus(EOrderStatus.FAILURE);
            orderRepository.save(order);

            Voucher voucher = voucherRepository
                    .existsByVoucherCode(voucherCode);
            if(voucher != null) {
                if(voucher.getStatus().equals(EVoucherStatus.INACTIVE) && voucher.getQuantity() == 0){
                    voucher.setQuantity(voucher.getQuantity() + 1);
                    voucher.setStatus(EVoucherStatus.ACTIVE);
                    voucherRepository.save(voucher);
                }
                else if(voucher.getStatus().equals(EVoucherStatus.ACTIVE) && voucher.getQuantity() > 0){
                    voucher.setQuantity(voucher.getQuantity() + 1);
                    voucherRepository.save(voucher);
                }
            }

            Account account = accountRepository
                    .findAccountByUserName(userName)
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

            List<OrderDetail> orderDetail = orderDetailRepository.findByOrderId(order.getOrderId());
            for(OrderDetail detail : orderDetail) {
                TicketPurchase ticketPurchase = ticketPurchaseRepository
                        .findById(detail.getTicketPurchase().getTicketPurchaseId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

                Optional<TicketPurchase> oldPurchaseOpt = ticketPurchaseRepository.findById(ticketPurchase.getTicketPurchaseOldId());
                if (oldPurchaseOpt.isPresent()) {
                    oldPurchaseOpt.get().setStatus(ETicketPurchaseStatus.PURCHASED);
                    ticketPurchase.setStatus(ETicketPurchaseStatus.CANCELLED);
                    // xử lý logic với oldPurchase
                }


                //Kiểm tra trạng thái của ticketPurchase xem thanh toán hay huỷ nếu không thì đi xuống dưới
                if (ticketPurchase.getStatus().equals(ETicketPurchaseStatus.PENDING)) {
                    //Nếu không ghế và có zone
                    if(ticketPurchase.getSeatActivity() == null && ticketPurchase.getZoneActivity() != null){
                        //kiểm tra Zone
                        ZoneActivity zoneActivity = zoneActivityRepository
                                .findByEventActivityIdAndZoneId(ticketPurchase.getZoneActivity().getEventActivity().getEventActivityId(), ticketPurchase.getZoneActivity().getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                        Zone zone = zoneRepository
                                .findById(ticketPurchase.getZoneActivity().getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                        zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() + ticketPurchase.getQuantity());
                        zoneActivityRepository.save(zoneActivity);

                        List<ZoneActivity> zoneActivities = zoneActivityRepository
                                .findByZoneId(ticketPurchase.getZoneActivity().getZone().getZoneId());
                        boolean allZoneUnavailable = zoneActivities.stream()
                                .allMatch(zoneActivity1 -> zoneActivity.getAvailableQuantity() == 0);
                        if (!allZoneUnavailable) {
                            zone.setStatus(true);
                        }

                        ticketPurchase.setStatus(ETicketPurchaseStatus.CANCELLED);

                        ticketPurchaseRepository.save(ticketPurchase);
                        zoneRepository.save(zone);
                    }

                    //Nếu có ghế và có zone
                    if(ticketPurchase.getZoneActivity() != null && ticketPurchase.getSeatActivity() != null){
                        //kiểm tra Zone
                        ZoneActivity zoneActivity = zoneActivityRepository
                                .findByEventActivityIdAndZoneId(ticketPurchase.getZoneActivity().getEventActivity().getEventActivityId(), ticketPurchase.getZoneActivity().getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                        Zone zone = zoneRepository
                                .findById(ticketPurchase.getZoneActivity().getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                        zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() + ticketPurchase.getQuantity());
                        zoneActivityRepository.save(zoneActivity);

                        List<ZoneActivity> zoneActivities = zoneActivityRepository
                                .findByZoneId(ticketPurchase.getZoneActivity().getZone().getZoneId());
                        boolean allZoneUnavailable = zoneActivities.stream()
                                .allMatch(zoneActivity1 -> zoneActivity.getAvailableQuantity() == 0);
                        if (!allZoneUnavailable) {
                            zone.setStatus(true);
                        }

                        //Kiểm tra Seat
                        SeatActivity seatActivity = seatActivityRepository
                                .findByEventActivityIdAndSeatId(ticketPurchase.getSeatActivity().getEventActivity().getEventActivityId(), ticketPurchase.getSeatActivity().getSeat().getSeatId())
                                .orElseThrow(() -> new AppException(ErrorCode.SEAT_ACTIVITY_NOT_FOUND));

                        Seat seat = seatRepository
                                .findById(ticketPurchase.getSeatActivity().getSeat().getSeatId())
                                .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));

                        seatActivity.setStatus(ESeatActivityStatus.AVAILABLE);
                        seatActivityRepository.save(seatActivity);

                        List<SeatActivity> seatActivities = seatActivityRepository.findBySeatId(ticketPurchase.getSeatActivity().getSeat().getSeatId());
                        boolean allSeatUnavailable = seatActivities.stream()
                                .allMatch(seatActivity1 -> seatActivity.getStatus().equals(ESeatActivityStatus.SOLD));
                        if (!allSeatUnavailable) {
                            seat.setStatus(true);
                        }

                        ticketPurchase.setStatus(ETicketPurchaseStatus.CANCELLED);

                        seatRepository.save(seat);
                        ticketPurchaseRepository.save(ticketPurchase);
                        zoneRepository.save(zone);
                    }

                    //Nếu không ghế và không zone
                    if(ticketPurchase.getZoneActivity() == null && ticketPurchase.getSeatActivity() == null){
                        TicketMapping ticketMapping = ticketMappingRepository
                                .findTicketMappingByTicketIdAndEventActivityId(ticketPurchase.getTicket().getTicketId(), ticketPurchase.getEventActivity().getEventActivityId())
                                .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                        if(ticketMapping.getQuantity() == 0) {
                            ticketMapping.setQuantity(ticketPurchase.getQuantity());
                            ticketMapping.setStatus(true);
                        }else {
                            ticketMapping.setQuantity(ticketMapping.getQuantity() + ticketPurchase.getQuantity());
                        }

                        ticketMappingRepository.save(ticketMapping);

                        ticketPurchase.setStatus(ETicketPurchaseStatus.CANCELLED);

                        ticketPurchaseRepository.save(ticketPurchase);
                    }
                }
            }
            Transaction transactionExisted = transactionRepository
                    .findByTransactionCode(transactionNo);

            if(transactionExisted != null){
                throw new AppException(ErrorCode.TRANSACTION_EXISTED);
            }

            Transaction transaction = new Transaction();
            transaction.setAmount(Double.valueOf(amount));
            transaction.setTransactionCode(transactionNo);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setDescription("Thanh toan don hang: " + orderCode);
            transaction.setAccount(account);
            transaction.setPayment(payment);
            transaction.setStatus(ETransactionStatus.FAILED);
            transaction.setContractPayment(null);
            transaction.setType(ETransactionType.DIRECT_PAYMENT);
            transactionRepository.save(transaction);
            return new PaymentResponse(status, "CANCELLED", mapper.map(payment, PaymentResponse.class));
        }
    }
        private String convert(String input) {
            // Giả sử input là định dạng như: "1743505747899-r0-c0"
            String[] parts = input.split("-");
            if (parts.length < 3) return input;

            String row = parts[1]; // r0
            String col = parts[2]; // c0

            int rowIndex = Integer.parseInt(row.substring(1)); // 0
            char rowChar = (char) ('A' + rowIndex); // 'A'

            int colIndex = Integer.parseInt(col.substring(1)) + 1; // 1

            return "r" + rowChar + "-c" + colIndex; // rA-c1
        }


    @Override
    public List<PaymentDTO> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .map(payment -> mapper.map(payment, PaymentDTO.class))
                .toList();
    }

    @Override
    public String testQR(TicketQrCodeDTO ticketQrCodeDTO) throws Exception {
        return generateQRCode(ticketQrCodeDTO);
    }

    private String generateQRCode(TicketQrCodeDTO ticketQrCodeDTO) throws Exception {
        String dataTransfer = AppUtils.transferToString(ticketQrCodeDTO);
        return AppUtils.encrypt(dataTransfer);
    }

    private String getBaseUrl(HttpServletRequest request) {
        return "http://localhost:5173";
    }
}
