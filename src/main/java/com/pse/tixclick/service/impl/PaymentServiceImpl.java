package com.pse.tixclick.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.PaymentDTO;
import com.pse.tixclick.payload.dto.TicketQrCodeDTO;
import com.pse.tixclick.payload.dto.ZoneDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.CheckinLog;
import com.pse.tixclick.payload.entity.entity_enum.*;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.payment.Order;
import com.pse.tixclick.payload.entity.payment.OrderDetail;
import com.pse.tixclick.payload.entity.payment.Payment;
import com.pse.tixclick.payload.entity.payment.Transaction;
import com.pse.tixclick.payload.entity.seatmap.*;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import com.pse.tixclick.payload.response.PayOSResponse;
import com.pse.tixclick.payload.response.PaymentResponse;
import com.pse.tixclick.payos.PayOSUtils;
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
import org.springframework.stereotype.Service;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private static final int QR_CODE_WIDTH = 600;
    private static final int QR_CODE_HEIGHT = 600;

    @Autowired
    PayOSUtils payOSUtils;

    @Autowired
    ModelMapper mapper;

    @Autowired
    AppUtils appUtils;

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


    @Override
    public PayOSResponse changeOrderStatusPayOs(int orderId) {
        return null;
    }

    @Override
    public PayOSResponse createPaymentLink(int orderId, HttpServletRequest request) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        Account account = accountRepository
                .findById(order.getAccount().getAccountId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        List<Payment> paymentHistory = paymentRepository.findByOrderId(order.getOrderId());
        if (!paymentHistory.isEmpty() && paymentHistory.stream().anyMatch(payment -> payment.getStatus().equals("SUCCESSFUL"))) {
            throw new RuntimeException("Payment is already completed");
        }

        int totalAmount = (int) Math.round(order.getTotalAmount());
        long orderCode = Long.parseLong(order.getOrderCode());
        String baseUrl = getBaseUrl(request);

        ItemData itemData = ItemData.builder()
                .name(order.getOrderCode())
                .quantity(1)
                .price(totalAmount)
                .build();

        // Xây dựng returnUrl và cancelUrl
        String returnUrl = baseUrl + "/payment-return" +
                "?orderId=" + order.getOrderId() +
                "&userName=" + account.getUserName() +
                "&amount=" + itemData.getPrice() +
                "&name=" + itemData.getName();

        String cancelUrl = baseUrl + "/payment-return"  +
                "?orderId=" + order.getOrderId() +
                "&userName=" + account.getUserName() +
                "&amount=" + itemData.getPrice() +
                "&name=" + itemData.getName();

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(totalAmount)
                .description("Thanh toán đơn hàng")
                .returnUrl(returnUrl)  // Sử dụng returnUrl đã xây dựng
                .cancelUrl(cancelUrl)  // Sử dụng cancelUrl đã xây dựng
                .item(itemData)
                .build();

        CheckoutResponseData result = payOSUtils.payOS().createPaymentLink(paymentData);

        Payment payment = new Payment();
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(EPaymentStatus.PENDING.name());
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
    public PaymentResponse handleCallbackPayOS(HttpServletRequest request) throws Exception {
        String status = request.getParameter("status");
        String code = request.getParameter("code");
        String orderId = request.getParameter("orderId");
        String transactionNo = request.getParameter("id");
        String userName = request.getParameter("userName");
        String orderCode = request.getParameter("orderCode");
        String amount = request.getParameter("amount");

        Payment payment = paymentRepository.findPaymentByOrderCode(orderCode);

        if (code.equals("00") && status.equals("PAID")) {
            payment.setStatus(EPaymentStatus.SUCCESSFULLY.name());
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(payment);

            Order order = orderRepository
                    .findById(Integer.valueOf(orderId))
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

            Account account = accountRepository
                    .findAccountByUserName(userName)
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

            List<OrderDetail> orderDetail = orderDetailRepository.findByOrderId(order.getOrderId());

            for(OrderDetail detail : orderDetail) {
                TicketPurchase ticketPurchase = ticketPurchaseRepository
                        .findById(detail.getTicketPurchase().getTicketPurchaseId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

                EventActivity eventActivity = eventActivityRepository
                        .findById(ticketPurchase.getEventActivity().getEventActivityId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

                Ticket ticket = ticketRepository
                        .findById(ticketPurchase.getTicket().getTicketId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

                Event event = eventRepository
                        .findById(ticketPurchase.getEvent().getEventId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

                Seat seat = seatRepository
                        .findById(ticketPurchase.getSeatActivity().getSeat().getSeatId())
                        .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));

                SeatActivity seatActivity = seatActivityRepository
                        .findByEventActivityIdAndSeatId(ticketPurchase.getZoneActivity().getEventActivity().getEventActivityId(), ticketPurchase.getSeatActivity().getSeat().getSeatId())
                        .orElseThrow(() -> new AppException(ErrorCode.SEAT_ACTIVITY_NOT_FOUND));

                Zone zone = zoneRepository
                        .findById(ticketPurchase.getZoneActivity().getZone().getZoneId())
                        .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                CheckinLog checkinLog = new CheckinLog();
                checkinLog.setCheckinTime(null);
                checkinLog.setCheckinDevice("Mobile");
                checkinLog.setTicketPurchase(ticketPurchase);
                checkinLog.setAccount(account);
                checkinLog.setCheckinLocation(event.getLocation());
                checkinLog.setCheckinStatus(ECheckinLogStatus.PENDING.name());
                checkinLogRepository.save(checkinLog);

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
                ticketQrCodeDTO.setCheckin_Log_id(checkinLog.getCheckinId());

                String qrCode = generateQRCode(ticketQrCodeDTO);
                ticketPurchase.setStatus(ETicketPurchaseStatus.PURCHASED);
                ticketPurchase.setQrCode(qrCode);
                ticketPurchaseRepository.save(ticketPurchase);

                seatActivity.setStatus(ESeatActivityStatus.SOLD.name());
                seatActivityRepository.save(seatActivity);
            }

            order.setStatus(EOrderStatus.SUCCESSFUL.name());
            orderRepository.save(order);

            Transaction transaction = new Transaction();
            transaction.setAmount(Double.valueOf(amount));
            transaction.setTransactionCode(transactionNo);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setDescription("Thanh toan don hang: " + orderCode);
            transaction.setAccount(account);
            transaction.setPayment(payment);
            transaction.setStatus(ETransactionStatus.SUCCESS.name());
            transaction.setContractPayment(null);
            transaction.setType(ETransactionType.DIRECT_PAYMENT.name());
            transactionRepository.save(transaction);

            return new PaymentResponse(status, "SUCCESSFUL", mapper.map(payment, PaymentResponse.class));
        }
        else {
            payment.setStatus(EPaymentStatus.FAILURE.name());
            paymentRepository.save(payment);

            Order order = orderRepository
                    .findById(Integer.valueOf(orderId))
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
            order.setStatus(EOrderStatus.FAILURE.name());
            orderRepository.save(order);

            Account account = accountRepository
                    .findAccountByUserName(userName)
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

            List<OrderDetail> orderDetail = orderDetailRepository.findByOrderId(order.getOrderId());
            for(OrderDetail detail : orderDetail) {
                TicketPurchase ticketPurchase = ticketPurchaseRepository
                        .findById(detail.getTicketPurchase().getTicketPurchaseId())
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));
                ticketPurchase.setStatus(ETicketPurchaseStatus.CANCELLED);

                SeatActivity seatActivity = seatActivityRepository
                        .findByEventActivityIdAndSeatId(ticketPurchase.getZoneActivity().getEventActivity().getEventActivityId(), ticketPurchase.getSeatActivity().getSeat().getSeatId())
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

                zoneRepository.save(zone);
                zoneActivityRepository.save(zoneActivity);
                ticketPurchaseRepository.save(ticketPurchase);

                Transaction transaction = new Transaction();
                transaction.setAmount(Double.valueOf(amount));
                transaction.setTransactionCode(transactionNo);
                transaction.setTransactionDate(LocalDateTime.now());
                transaction.setDescription("Thanh toan don hang: " + orderCode);
                transaction.setAccount(account);
                transaction.setPayment(payment);
                transaction.setStatus(ETransactionStatus.FAILED.name());
                transaction.setContractPayment(null);
                transaction.setType(ETransactionType.DIRECT_PAYMENT.name());
                transactionRepository.save(transaction);
            }
            return new PaymentResponse(status, "CANCELLED", mapper.map(payment, PaymentResponse.class));
        }
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
        String encryptedData = AppUtils.encrypt(dataTransfer); // Mã hóa dữ liệu
        return AppUtils.generateQRCode(encryptedData, QR_CODE_WIDTH, QR_CODE_HEIGHT);
    }

    private String getBaseUrl(HttpServletRequest request) {
        return "http://tixclick.site";
    }

    private Date setTransactionDateFromPayDate(String payDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        try {
            return formatter.parse(payDate);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid date format for payDate: " + payDate);
        }
    }
}
