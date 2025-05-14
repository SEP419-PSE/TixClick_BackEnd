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
import com.pse.tixclick.payload.request.TicketPurchaseRequest;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;
import com.pse.tixclick.payload.response.PayOSResponse;
import com.pse.tixclick.payload.response.PaymentResponse;
import com.pse.tixclick.payment.PayOSUtils;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.PaymentService;
import com.pse.tixclick.service.TicketPurchaseService;
import com.pse.tixclick.utils.AppUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
    @Autowired
    TicketPurchaseServiceImpl ticketPurchaseService;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

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

        String fullname = order.getAccount().getFirstName() + " " + order.getAccount().getLastName();
        List<TicketPurchase> ticketPurchases = ticketPurchaseRepository.findTicketPurchaseByOrderCode(order.getOrderCode());

        List<String> seatDescriptions = ticketPurchases.stream()
                .map(TicketPurchase::getSeatActivity)
                .filter(Objects::nonNull)
                .map(AppUtils::parseSeatDisplayName)
                .toList();

        String fullSeatInfo = String.join("; ", seatDescriptions);

        // Gộp lại để truyền vào description
        String description = "Thanh toán đơn hàng cho " + fullname + " - " + fullSeatInfo;




        Payment payment = new Payment();
        payment.setAmount(totalAmount);
        payment.setStatus(EPaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setOrderCode(order.getOrderCode());
        payment.setOrder(order);
        payment.setPaymentMethod(description);
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
    public     PayOSResponse changTicket(List<TicketPurchaseRequest> ticketPurchaseRequests, List<CreateTicketPurchaseRequest> ticketChange, String orderCode, HttpServletRequest request) throws Exception
    {
        var context = SecurityContextHolder.getContext();
        var userName = context.getAuthentication().getName();
        int i;
        int count =0;
        var account = accountRepository.findAccountByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        Order oldOrder = orderRepository.findOrderByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (oldOrder.getAccount().getAccountId() != account.getAccountId()) {
            throw new AppException(ErrorCode.USER_NOT_BUYER);
        }

        if (!oldOrder.getStatus().equals(EOrderStatus.SUCCESSFUL)) {
            throw new AppException(ErrorCode.ORDER_CANCELLED);
        }

        List<TicketPurchase> ticketPurchases = ticketPurchaseRepository.findTicketPurchaseByOrderCode(orderCode);
        if (ticketPurchases.isEmpty()) {
            throw new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND);
        }

        double totalAmount = 0;
        double oldAmount = 0;
        List<TicketPurchase> newPurchases = new ArrayList<>();
        List<Integer> ticketPurchaseIds = new ArrayList<>();
        String orderNote = "Đổi vé";
        Set<Integer> processedOldTicketIds = new HashSet<>();

        // Validate ticketPurchaseRequests
        for (TicketPurchaseRequest req : ticketPurchaseRequests) {
            if (req.getTicketPurchaseId() == 0) {
                throw new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND);
            }

            var ticketPurchase = ticketPurchaseRepository.findById(req.getTicketPurchaseId())
                    .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

            if (!ticketPurchases.contains(ticketPurchase)) {
                throw new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND);
            }

            if (ticketPurchase.getStatus().equals(ETicketPurchaseStatus.PENDING)) {
                throw new AppException(ErrorCode.TICKET_PURCHASE_DONT_BUY);
            }

            if (ticketPurchase.getStatus().equals(ETicketPurchaseStatus.CANCELLED)) {
                throw new AppException(ErrorCode.TICKET_PURCHASE_CANCELLED);
            }
        }

        // Process ticket changes
        int totalRequestedQuantity = ticketChange.stream()
                .mapToInt(CreateTicketPurchaseRequest::getQuantity)
                .sum();
        int totalOriginalQuantity = ticketPurchaseRequests.stream()
                .mapToInt(req -> ticketPurchaseRepository.findById(req.getTicketPurchaseId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND))
                        .getQuantity())
                .sum();

        if (totalRequestedQuantity > totalOriginalQuantity) {
            throw new AppException(ErrorCode.TICKET_REQUEST_EXCEED_QUANTITY);
        }

        if (ticketChange.size() != ticketPurchaseRequests.size()) {
            throw new AppException(ErrorCode.INVALID_REQUEST); // kiểm tra dữ liệu đầu vào
        }

        for ( i = 0; i < ticketChange.size(); i++) {
            CreateTicketPurchaseRequest ticketReq = ticketChange.get(i);
            TicketPurchaseRequest oldPurchaseReq = ticketPurchaseRequests.get(i); // giữ đúng theo thứ tự

            Ticket newTicket = ticketRepository.findById(ticketReq.getTicketId())
                    .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

            TicketPurchase oldPurchase = ticketPurchaseRepository
                    .findTicketPurchaseByTicketPurchaseId(oldPurchaseReq.getTicketPurchaseId())
                    .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));
            double oldTicketPrice = oldPurchase.getTicket().getPrice() * oldPurchase.getQuantity();
            oldAmount += oldTicketPrice;

            TicketPurchase newPurchase = new TicketPurchase();
            newPurchase.setAccount(account);
            newPurchase.setEventActivity(oldPurchase.getEventActivity());
            newPurchase.setEvent(oldPurchase.getEvent());
            newPurchase.setQuantity(ticketReq.getQuantity());
            newPurchase.setTicket(newTicket);
            newPurchase.setStatus(ETicketPurchaseStatus.PENDING);
            double amount = newTicket.getPrice() * ticketReq.getQuantity();
            totalAmount += amount;


            // ✅ Luôn gán ticketPurchaseOldId theo từng dòng tương ứng, bất kể ID có trùng
            newPurchase.setTicketPurchaseOldId(oldPurchase.getTicketPurchaseId());

            // xử lý mapping
            if (ticketReq.getTicketId() != 0 && ticketReq.getZoneId() == 0 && ticketReq.getSeatId() == 0) {
                TicketMapping ticketMapping = ticketMappingRepository
                        .findTicketMappingByTicketIdAndEventActivityId(newTicket.getTicketId(), ticketReq.getEventActivityId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));
                if (ticketMapping.getQuantity() < ticketReq.getQuantity()) {
                    throw new AppException(ErrorCode.TICKET_NOT_ENOUGH);
                }
                ticketMapping.setQuantity(ticketMapping.getQuantity() - ticketReq.getQuantity());
                ticketMappingRepository.save(ticketMapping);
            }

            // xử lý ghế
            if (ticketReq.getSeatId() != 0 && ticketReq.getZoneId() != 0) {
                SeatActivity newSeatActivity = seatActivityRepository
                        .findByEventActivityIdAndSeatId(ticketReq.getEventActivityId(), ticketReq.getSeatId())
                        .orElseThrow(() -> new AppException(ErrorCode.SEAT_ACTIVITY_NOT_FOUND));
                if (newSeatActivity.getStatus() != ESeatActivityStatus.AVAILABLE) {
                    throw new AppException(ErrorCode.SEAT_ALREADY_BOOKED);
                }
                newPurchase.setSeatActivity(newSeatActivity);
                newPurchase.setZoneActivity(newSeatActivity.getZoneActivity());
                newSeatActivity.setStatus(ESeatActivityStatus.SOLD);
                seatActivityRepository.save(newSeatActivity);
            } else if (ticketReq.getZoneId() != 0 && ticketReq.getSeatId() == 0) {
                ZoneActivity newZoneActivity = zoneActivityRepository
                        .findByEventActivityIdAndZoneId(ticketReq.getEventActivityId(), ticketReq.getZoneId())
                        .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));
                if (newZoneActivity.getAvailableQuantity() < ticketReq.getQuantity()) {
                    throw new AppException(ErrorCode.TICKET_NOT_ENOUGH);
                }
                newZoneActivity.setAvailableQuantity(newZoneActivity.getAvailableQuantity() - ticketReq.getQuantity());
                zoneActivityRepository.save(newZoneActivity);
                newPurchase.setZoneActivity(newZoneActivity);
            }

            newPurchase.setOrderCode(oldPurchase.getOrderCode());
            newPurchase = ticketPurchaseRepository.save(newPurchase);
            newPurchases.add(newPurchase);
            ticketPurchaseIds.add(newPurchase.getTicketPurchaseId());
            count = count + 1;
        }


        // Handle remaining quantity
        int remainingQuantity = totalOriginalQuantity - totalRequestedQuantity;
        if (remainingQuantity > 0) {
            for (TicketPurchaseRequest req : ticketPurchaseRequests) {
                TicketPurchase oldPurchase = ticketPurchaseRepository.findTicketPurchaseByTicketPurchaseId(req.getTicketPurchaseId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));


                TicketPurchase keepPurchase = new TicketPurchase();
                keepPurchase.setAccount(account);
                keepPurchase.setEventActivity(oldPurchase.getEventActivity());
                keepPurchase.setEvent(oldPurchase.getEvent());
                keepPurchase.setQuantity(remainingQuantity);
                keepPurchase.setTicket(oldPurchase.getTicket());
                keepPurchase.setZoneActivity(oldPurchase.getZoneActivity());
                keepPurchase.setSeatActivity(oldPurchase.getSeatActivity());
                keepPurchase.setStatus(ETicketPurchaseStatus.PENDING);

                TicketPurchaseRequest oldPurchaseReq = ticketPurchaseRequests.get(count);

                keepPurchase.setTicketPurchaseOldId(oldPurchaseReq.getTicketPurchaseId());
                count = count + 1;

                keepPurchase = ticketPurchaseRepository.save(keepPurchase);
                newPurchases.add(keepPurchase);
                ticketPurchaseIds.add(keepPurchase.getTicketPurchaseId());
            }
        }

        // Cancel old ticket purchases
        for (TicketPurchaseRequest req : ticketPurchaseRequests) {
            TicketPurchase ticketPurchase = ticketPurchaseRepository.findById(req.getTicketPurchaseId())
                    .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));
            ticketPurchase.setStatus(ETicketPurchaseStatus.CANCELLED);
            ticketPurchaseRepository.save(ticketPurchase);
        }

        // Update old order status
        oldOrder.setStatus(EOrderStatus.REFUNDED);
        orderRepository.save(oldOrder);

        // Schedule status update
        ticketPurchaseService.scheduleStatusUpdate(LocalDateTime.now(), ticketPurchaseIds);
        double priceDiff = totalAmount - oldAmount;

        // Áp dụng xử lý tối thiểu 10.000 nếu cần:
        if (priceDiff < 0) {
            priceDiff = 0;
        } else if (priceDiff > 0 && priceDiff < 10000) {
            priceDiff = 10000;
        }


        // Create new order
        Order newOrder = new Order();
        newOrder.setOrderCode(orderServiceImpl.orderCodeAutomationCreating());
        newOrder.setStatus(priceDiff == 0 ? EOrderStatus.SUCCESSFUL : EOrderStatus.PENDING);
        newOrder.setTotalAmount(priceDiff);
        newOrder.setTotalAmountDiscount(totalAmount);
        newOrder.setNote(orderNote);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setAccount(account);
        newOrder = orderRepository.save(newOrder);

        // Create order details
        for (TicketPurchase newPurchase : newPurchases) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(newOrder);
            orderDetail.setTicketPurchase(newPurchase);
            orderDetail.setAmount(newPurchase.getTicket().getPrice() * newPurchase.getQuantity());
            orderDetailRepository.save(orderDetail);
        }

        // Handle no payment case
        if (priceDiff == 0) {
            CheckinLog checkinLog = new CheckinLog();
            checkinLog.setCheckinTime(null);
            checkinLog.setCheckinDevice("Mobile");
            checkinLog.setOrder(newOrder);
            checkinLog.setCheckinStatus(ECheckinLogStatus.PENDING);
            checkinLog.setCheckinLocation(null);
            checkinLog.setStaff(null);
            checkinLog.setAccount(account);
            checkinLogRepository.save(checkinLog);

            TicketQrCodeDTO ticketQrCodeDTO = new TicketQrCodeDTO();
            ticketQrCodeDTO.setCheckin_Log_id(checkinLog.getCheckinId());
            ticketQrCodeDTO.setUser_name(account.getUserName());
            ticketQrCodeDTO.setEvent_activity_id(ticketPurchases.get(0).getEventActivity().getEventActivityId());
            ticketQrCodeDTO.setOrder_id(newOrder.getOrderId());
            String qrCode = generateQRCode(ticketQrCodeDTO);
            newOrder.setQrCode(qrCode);
            newOrder.setStatus(EOrderStatus.SUCCESSFUL);
            orderRepository.save(newOrder);

            for (TicketPurchase newPurchase : newPurchases) {
                var oldTicketId = newPurchase.getTicketPurchaseOldId();
                if (oldTicketId != null && !processedOldTicketIds.contains(oldTicketId)) {
                    processedOldTicketIds.add(oldTicketId);

                    TicketPurchase oldPurchase = ticketPurchaseRepository.findById(oldTicketId)
                            .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

                    if (oldPurchase.getSeatActivity() != null) {
                        oldPurchase.getSeatActivity().setStatus(ESeatActivityStatus.AVAILABLE);
                        seatActivityRepository.save(oldPurchase.getSeatActivity());
                    } else if (oldPurchase.getZoneActivity() != null) {
                        var zoneActivity = oldPurchase.getZoneActivity();
                        zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() + oldPurchase.getQuantity());
                        zoneActivityRepository.save(zoneActivity);
                    } else {
                        TicketMapping oldMapping = ticketMappingRepository
                                .findTicketMappingByTicketIdAndEventActivityId(
                                        oldPurchase.getTicket().getTicketId(),
                                        oldPurchase.getEventActivity().getEventActivityId())
                                .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));
                        oldMapping.setQuantity(oldMapping.getQuantity() + oldPurchase.getQuantity());
                        ticketMappingRepository.save(oldMapping);
                    }
                }

                newPurchase.setOrderCode(newOrder.getOrderCode());
                newPurchase.setStatus(ETicketPurchaseStatus.PURCHASED);
                ticketPurchaseRepository.save(newPurchase);
            }

            simpMessagingTemplate.convertAndSend("/all/messages", "call api");
            Transaction transaction = new Transaction();
            return PayOSResponse.builder()
                    .error("ok")
                    .message("Đổi vé thành công, không cần thanh toán")
                    .data(null)
                    .build();
        }

        // Xử lý thanh toán khi totalAmount > 0
        ObjectMapper objectMapper = new ObjectMapper();
        List<Payment> paymentHistory = paymentRepository.findByOrderId(newOrder.getOrderId());
        if (!paymentHistory.isEmpty() && paymentHistory.stream().anyMatch(payment -> payment.getStatus().equals(EPaymentStatus.SUCCESSFULLY))) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_COMPLETED);
        }

        int totalAmountInt = (int) Math.round(priceDiff);
        long neworderCode = Long.parseLong(newOrder.getOrderCode());
        String baseUrl = getBaseUrl(request);

        ItemData itemData = ItemData.builder()
                .name(newOrder.getOrderCode())
                .quantity(1)
                .price(totalAmountInt)
                .build();

        String returnUrl = baseUrl + "/payment/queue" +
                "?orderId=" + newOrder.getOrderId() +
                "&voucherCode=" + "" +
                "&userName=" + account.getUserName() +
                "&amount=" + itemData.getPrice() +
                "&name=" + itemData.getName();

        String cancelUrl = returnUrl;

        long expiredAt = Instant.now().getEpochSecond() + 900L;
        PaymentData paymentData = PaymentData.builder()
                .expiredAt(expiredAt)
                .orderCode(neworderCode)
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
        payment.setOrderCode(newOrder.getOrderCode());
        payment.setOrder(newOrder);
        payment.setPaymentMethod("Thanh toán ngân hàng");
        payment.setAccount(account);
        paymentRepository.save(payment);

        simpMessagingTemplate.convertAndSend("/all/messages", "call api");

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

            CheckinLog checkinLog = new CheckinLog();
            checkinLog.setCheckinTime(null);
            checkinLog.setCheckinDevice("Mobile");
            checkinLog.setOrder(order);
            checkinLog.setCheckinStatus(ECheckinLogStatus.PENDING);
            checkinLog.setCheckinLocation(null);
            checkinLog.setStaff(null);
            checkinLog.setAccount(order.getAccount());
            checkinLogRepository.save(checkinLog);

            TicketQrCodeDTO ticketQrCodeDTO = new TicketQrCodeDTO();
            ticketQrCodeDTO.setCheckin_Log_id(checkinLog.getCheckinId());
            ticketQrCodeDTO.setUser_name(userName);
            ticketQrCodeDTO.setEvent_activity_id(order.getOrderDetails().stream()
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_DETAIL_NOT_FOUND))
                    .getTicketPurchase().getEventActivity().getEventActivityId());
            ticketQrCodeDTO.setOrder_id(order.getOrderId());
            String qrCode = generateQRCode(ticketQrCodeDTO);

            ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
            LocalDateTime vietnamTime = LocalDateTime.now(vietnamZone);
            order.setOrderDate(vietnamTime);
            order.setQrCode(qrCode);
            order.setStatus(EOrderStatus.SUCCESSFUL);
            orderRepository.save(order);

            Account account = accountRepository
                    .findAccountByUserName(userName)
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

            List<OrderDetail> orderDetail = orderDetailRepository.findByOrderId(order.getOrderId());

            for (OrderDetail detail : orderDetail) {
                TicketPurchase ticketPurchase = ticketPurchaseRepository
                        .findById(detail.getTicketPurchase().getTicketPurchaseId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

                if (ticketPurchase.getTicketPurchaseOldId() != null) {
                    Optional<TicketPurchase> oldTicketPurchase = ticketPurchaseRepository
                            .findTicketPurchaseByTicketPurchaseId(ticketPurchase.getTicketPurchaseOldId());

                    if (oldTicketPurchase.isPresent()) {
                        oldTicketPurchase.get().setStatus(ETicketPurchaseStatus.CANCELLED);
                        ticketPurchaseRepository.save(oldTicketPurchase.get());

                        if (oldTicketPurchase.get().getZoneActivity() == null && oldTicketPurchase.get().getTicket() != null) {
                            TicketMapping ticketMapping = ticketMappingRepository.findTicketMappingByTicketIdAndEventActivityId(
                                    oldTicketPurchase.get().getTicket().getTicketId(),
                                    oldTicketPurchase.get().getEventActivity().getEventActivityId()
                            ).orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                            ticketMapping.setQuantity(ticketMapping.getQuantity() + ticketPurchase.getQuantity());
                            ticketMappingRepository.save(ticketMapping);
                        } else if (oldTicketPurchase.get().getSeatActivity() == null && oldTicketPurchase.get().getZoneActivity() != null && oldTicketPurchase.get().getTicket() != null) {
                            ZoneActivity zoneActivity = oldTicketPurchase.get().getZoneActivity();
                            zoneActivity.setAvailableQuantity(zoneActivity.getAvailableQuantity() + ticketPurchase.getQuantity());
                            zoneActivityRepository.save(zoneActivity);
                        } else {
                            SeatActivity seatActivity = oldTicketPurchase.get().getSeatActivity();
                            seatActivity.setStatus(ESeatActivityStatus.AVAILABLE);
                            seatActivityRepository.save(seatActivity);
                        }
                    }
                }

                if (ticketPurchase.getStatus().equals(ETicketPurchaseStatus.PENDING) ||
                        ticketPurchase.getStatus().equals(ETicketPurchaseStatus.PURCHASED)) {
                    if (ticketPurchase.getSeatActivity() == null && ticketPurchase.getZoneActivity() != null) {
                        ZoneActivity zoneActivity = zoneActivityRepository
                                .findByEventActivityIdAndZoneId(ticketPurchase.getZoneActivity().getEventActivity().getEventActivityId(), ticketPurchase.getZoneActivity().getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                        Zone zone = zoneRepository
                                .findById(ticketPurchase.getZoneActivity().getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                        List<ZoneActivity> zoneActivities = zoneActivityRepository
                                .findByZoneId(ticketPurchase.getZoneActivity().getZone().getZoneId());
                        boolean allZoneUnavailable = zoneActivities.stream()
                                .allMatch(zoneActivity1 -> zoneActivity1.getAvailableQuantity() == 0);
                        if (allZoneUnavailable) {
                            zone.setStatus(false);
                        }

                        zoneRepository.save(zone);
                        zoneActivityRepository.save(zoneActivity);
                    }

                    if (ticketPurchase.getZoneActivity() != null && ticketPurchase.getSeatActivity() != null) {
                        ZoneActivity zoneActivity = zoneActivityRepository
                                .findByEventActivityIdAndZoneId(ticketPurchase.getZoneActivity().getEventActivity().getEventActivityId(), ticketPurchase.getZoneActivity().getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_ACTIVITY_NOT_FOUND));

                        Zone zone = zoneRepository
                                .findById(ticketPurchase.getZoneActivity().getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                        List<ZoneActivity> zoneActivities = zoneActivityRepository
                                .findByZoneId(ticketPurchase.getZoneActivity().getZone().getZoneId());
                        boolean allZoneUnavailable = zoneActivities.stream()
                                .allMatch(zoneActivity1 -> zoneActivity1.getAvailableQuantity() == 0);
                        if (allZoneUnavailable) {
                            zone.setStatus(false);
                        }

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
                                .allMatch(seatActivity1 -> seatActivity1.getStatus().equals(ESeatActivityStatus.SOLD));
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

                ticketPurchase.setStatus(ETicketPurchaseStatus.PURCHASED);
                ticketPurchase.setOrderCode(order.getOrderCode());
                ticketPurchaseRepository.save(ticketPurchase);
            }

            Transaction transactionExisted = transactionRepository
                    .findByTransactionCode(transactionNo);

            if (transactionExisted != null) {
                throw new AppException(ErrorCode.TRANSACTION_EXISTED);
            }

            Transaction transaction = new Transaction();
            transaction.setAmount(Double.valueOf(amount));
            transaction.setTransactionCode(transactionNo);
            transaction.setTransactionDate(vietnamTime);
            transaction.setDescription("Thanh toan don hang: " + orderCode);
            transaction.setAccount(account);
            transaction.setPayment(payment);
            transaction.setStatus(ETransactionStatus.SUCCESS);
            transaction.setContractPayment(null);
            transaction.setType(ETransactionType.DIRECT_PAYMENT);
            transactionRepository.save(transaction);

            simpMessagingTemplate.convertAndSend("/all/messages", "call api");
            return new PaymentResponse(status, "SUCCESSFUL", mapper.map(payment, PaymentResponse.class));
        } else {
            payment.setStatus(EPaymentStatus.FAILURE);
            paymentRepository.save(payment);

            Order order = orderRepository
                    .findById(Integer.valueOf(orderId))
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

            order.setStatus(EOrderStatus.FAILURE);
            orderRepository.save(order);

            List<TicketPurchase> ticketPurchases = ticketPurchaseRepository
                    .findTicketPurchaseByOrderCode(order.getOrderCode());
            List<TicketPurchase> oldTicketPurchases = new ArrayList<>();
            String oldOrderCode = null;
            for (TicketPurchase ticketPurchase : ticketPurchases) {
                var oldticket = ticketPurchase.getTicketPurchaseOldId();
                if (oldticket != null) {
                    oldTicketPurchases.add(ticketPurchase);
                }
            }
            if (!oldTicketPurchases.isEmpty()) {
                for (TicketPurchase ticketPurchase : oldTicketPurchases) {
                    TicketPurchase oldTicketPurchase = ticketPurchaseRepository
                            .findById(ticketPurchase.getTicketPurchaseOldId())
                            .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));
                    oldTicketPurchase.setStatus(ETicketPurchaseStatus.PURCHASED);
                    ticketPurchaseRepository.save(oldTicketPurchase);
                    oldOrderCode = ticketPurchase.getOrderCode();
                }
                Order oldOrder = orderRepository
                        .findOrderByOrderCode(oldOrderCode)
                        .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
                oldOrder.setStatus(EOrderStatus.SUCCESSFUL);
                orderRepository.save(oldOrder);
            }

            Voucher voucher = voucherRepository.existsByVoucherCode(voucherCode);
            if (voucher != null) {
                if (voucher.getStatus().equals(EVoucherStatus.INACTIVE) && voucher.getQuantity() == 0) {
                    voucher.setQuantity(voucher.getQuantity() + 1);
                    voucher.setStatus(EVoucherStatus.ACTIVE);
                    voucherRepository.save(voucher);
                } else if (voucher.getStatus().equals(EVoucherStatus.ACTIVE) && voucher.getQuantity() > 0) {
                    voucher.setQuantity(voucher.getQuantity() + 1);
                    voucherRepository.save(voucher);
                }
            }

            Account account = accountRepository
                    .findAccountByUserName(userName)
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

            List<OrderDetail> orderDetail = orderDetailRepository.findByOrderId(order.getOrderId());
            for (OrderDetail detail : orderDetail) {
                TicketPurchase ticketPurchase = ticketPurchaseRepository
                        .findById(detail.getTicketPurchase().getTicketPurchaseId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

                if (ticketPurchase.getTicketPurchaseOldId() != null) {
                    // Xử lý ticketPurchase cũ (nếu cần)
                }

                if (ticketPurchase.getStatus().equals(ETicketPurchaseStatus.PENDING)) {
                    if (ticketPurchase.getSeatActivity() == null && ticketPurchase.getZoneActivity() != null) {
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
                                .allMatch(zoneActivity1 -> zoneActivity1.getAvailableQuantity() == 0);
                        if (!allZoneUnavailable) {
                            zone.setStatus(true);
                        }

                        ticketPurchase.setStatus(ETicketPurchaseStatus.CANCELLED);
                        ticketPurchaseRepository.save(ticketPurchase);
                        zoneRepository.save(zone);
                    }

                    if (ticketPurchase.getZoneActivity() != null && ticketPurchase.getSeatActivity() != null) {
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
                                .allMatch(zoneActivity1 -> zoneActivity1.getAvailableQuantity() == 0);
                        if (!allZoneUnavailable) {
                            zone.setStatus(true);
                        }

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
                                .allMatch(seatActivity1 -> seatActivity1.getStatus().equals(ESeatActivityStatus.SOLD));
                        if (!allSeatUnavailable) {
                            seat.setStatus(true);
                        }

                        ticketPurchase.setStatus(ETicketPurchaseStatus.CANCELLED);
                        seatRepository.save(seat);
                        ticketPurchaseRepository.save(ticketPurchase);
                        zoneRepository.save(zone);
                    }

                    if (ticketPurchase.getZoneActivity() == null && ticketPurchase.getSeatActivity() == null) {
                        TicketMapping ticketMapping = ticketMappingRepository
                                .findTicketMappingByTicketIdAndEventActivityId(ticketPurchase.getTicket().getTicketId(), ticketPurchase.getEventActivity().getEventActivityId())
                                .orElseThrow(() -> new AppException(ErrorCode.TICKET_MAPPING_NOT_FOUND));

                        if (ticketMapping.getQuantity() == 0) {
                            ticketMapping.setQuantity(ticketPurchase.getQuantity());
                            ticketMapping.setStatus(true);
                        } else {
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

            if (transactionExisted != null) {
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

            simpMessagingTemplate.convertAndSend("/all/messages", "call api");
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
        return AppUtils.encryptTicketQrCode(ticketQrCodeDTO);
    }


    private String getBaseUrl(HttpServletRequest request) {
        return "https://tixclick.site";
    }
}
