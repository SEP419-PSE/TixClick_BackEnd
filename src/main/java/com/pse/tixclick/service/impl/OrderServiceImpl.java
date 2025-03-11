package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.*;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.entity_enum.EOrderStatus;
import com.pse.tixclick.payload.entity.entity_enum.ETicketPurchaseStatus;
import com.pse.tixclick.payload.entity.payment.Order;
import com.pse.tixclick.payload.entity.payment.OrderDetail;
import com.pse.tixclick.payload.entity.payment.Voucher;
import com.pse.tixclick.payload.entity.seatmap.Seat;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import com.pse.tixclick.payload.request.create.CreateOrderRequest;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.OrderService;
import com.pse.tixclick.utils.AppUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class OrderServiceImpl implements OrderService {
    @Autowired
    AppUtils appUtils;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderDetailRepository orderDetailRepository;

    @Autowired
    ModelMapper mapper;

    @Autowired
    ZoneRepository zoneRepository;

    @Autowired
    TicketPurchaseRepository ticketPurchaseRepository;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    VoucherRepository voucherRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);



    @Override
    public OrderDTO createOrder(CreateOrderRequest createOrderRequest) {
        Order order = new Order();
        order.setOrderCode(orderCodeAutomationCreating());
        order.setStatus(EOrderStatus.PENDING.name());
        order.setOrderDate(LocalDateTime.now());
        order.setAccount(appUtils.getAccountFromAuthentication());
        order.setTotalAmount(0);
        order = orderRepository.save(order);

        double totalAmount = 0;
        Set<Integer> ticketPurchaseIds = new HashSet<>();

        for (TicketOrderDTO ticketOrderDTO : createOrderRequest.getTicketOrderDTOS()) {
            int ticketPurchaseId = ticketOrderDTO.getTicketPurchaseId();
            int quantity = ticketOrderDTO.getQuantity();

            if (!ticketPurchaseIds.add(ticketPurchaseId)) {
                throw new AppException(ErrorCode.DUPLICATE_TICKET_PURCHASE);
            }

            TicketPurchase ticketPurchase = ticketPurchaseRepository
                    .findById(ticketPurchaseId)
                    .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));
            if(ticketPurchase.getStatus().equals(ETicketPurchaseStatus.EXPIRED.name())) {
                throw new AppException(ErrorCode.TICKET_PURCHASE_EXPIRED);
            }

            Ticket ticket = ticketRepository.findById(ticketPurchase.getTicket().getTicketId())
                    .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

            int minQuantity = ticket.getMinQuantity();
            int maxQuantity = ticket.getMaxQuantity();

            if (quantity < minQuantity || quantity > maxQuantity) {
                throw new AppException(ErrorCode.INVALID_QUANTITY);
            }

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setTicketPurchase(ticketPurchase);

            double amount = ticket.getPrice() * quantity;

            if (createOrderRequest.getVoucherCode() != null && !createOrderRequest.getVoucherCode().isEmpty()) {
                Voucher voucher = voucherRepository.findByVoucherCode(createOrderRequest.getVoucherCode())
                        .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
                orderDetail.setVoucher(voucher);
                amount *= voucher.getDiscount();
            }

            orderDetail.setAmount(amount);
            totalAmount += amount;

            orderDetailRepository.save(orderDetail);
        }

        order.setTotalAmount(totalAmount);
        orderRepository.save(order);

        final int orderId = order.getOrderId();
        CompletableFuture.runAsync(() -> scheduleStatusUpdate(LocalDateTime.now(), orderId));

        return mapper.map(order, OrderDTO.class);
    }

    @Override
    public List<Order_OrderDetailDTO> getAllOrderOfUser() {
        Account account = appUtils.getAccountFromAuthentication();
        List<Order> orders = orderRepository.findByAccountId(account.getAccountId());

        return orders.stream()
                .map(order -> {
                    Order_OrderDetailDTO dto = mapper.map(order, Order_OrderDetailDTO.class);

                    List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getOrderId());
                    if (!orderDetails.isEmpty()) {
                        List<OrderDetailDTO> orderDetailDTOs = orderDetails.stream()
                                .map(orderDetail -> {
                                    OrderDetailDTO orderDetailDTO = mapper.map(orderDetail, OrderDetailDTO.class);

                                    if (orderDetail.getTicketPurchase() != null) {
                                        TicketPurchase ticketPurchase = orderDetail.getTicketPurchase();
                                        TicketPurchaseDTO ticketPurchaseDTO = new TicketPurchaseDTO();

                                        ticketPurchaseDTO.setTicketPurchaseId(ticketPurchase.getTicketPurchaseId());
                                        ticketPurchaseDTO.setQrCode(ticketPurchase.getQrCode());
                                        ticketPurchaseDTO.setStatus(ticketPurchase.getStatus());

                                        if (ticketPurchase.getTicket() != null) {
                                            ticketPurchaseDTO.setTicketId(ticketPurchase.getTicket().getTicketId());
                                        }

                                        if (ticketPurchase.getEventActivity() != null) {
                                            ticketPurchaseDTO.setEventActivityId(ticketPurchase.getEventActivity().getEventActivityId());
                                        }

                                        if (ticketPurchase.getZone() != null) {
                                            ticketPurchaseDTO.setZoneId(ticketPurchase.getZone().getZoneId());
                                        }

                                        if (ticketPurchase.getSeat() != null) {
                                            ticketPurchaseDTO.setSeatId(ticketPurchase.getSeat().getSeatId());
                                        }

                                        if (ticketPurchase.getEvent() != null) {
                                            ticketPurchaseDTO.setEventId(ticketPurchase.getEvent().getEventId());
                                        }
                                        orderDetailDTO.setTicketPurchaseDTO(ticketPurchaseDTO);
                                    }
                                    return orderDetailDTO;
                                })
                                .collect(Collectors.toList());
                        dto.setOrderDetail(orderDetailDTOs);
                    }
                    return dto;
                })
                .toList();
    }

    @Override
    public List<Order_OrderDetailDTO> getOrderStatusOfUser(String status) {
        Account account = appUtils.getAccountFromAuthentication();
        List<Order> orders = orderRepository.findByStatusOfAccount(account.getAccountId(), status);

        return orders.stream()
                .map(order -> {
                    Order_OrderDetailDTO dto = mapper.map(order, Order_OrderDetailDTO.class);

                    List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getOrderId());
                    if (!orderDetails.isEmpty()) {
                        List<OrderDetailDTO> orderDetailDTOs = orderDetails.stream()
                                .map(orderDetail -> {
                                    OrderDetailDTO orderDetailDTO = mapper.map(orderDetail, OrderDetailDTO.class);

                                    if (orderDetail.getTicketPurchase() != null) {
                                        TicketPurchase ticketPurchase = orderDetail.getTicketPurchase();
                                        TicketPurchaseDTO ticketPurchaseDTO = new TicketPurchaseDTO();

                                        ticketPurchaseDTO.setTicketPurchaseId(ticketPurchase.getTicketPurchaseId());
                                        ticketPurchaseDTO.setQrCode(ticketPurchase.getQrCode());
                                        ticketPurchaseDTO.setStatus(ticketPurchase.getStatus());

                                        if (ticketPurchase.getTicket() != null) {
                                            ticketPurchaseDTO.setTicketId(ticketPurchase.getTicket().getTicketId());
                                        }

                                        if (ticketPurchase.getEventActivity() != null) {
                                            ticketPurchaseDTO.setEventActivityId(ticketPurchase.getEventActivity().getEventActivityId());
                                        }

                                        if (ticketPurchase.getZone() != null) {
                                            ticketPurchaseDTO.setZoneId(ticketPurchase.getZone().getZoneId());
                                        }

                                        if (ticketPurchase.getSeat() != null) {
                                            ticketPurchaseDTO.setSeatId(ticketPurchase.getSeat().getSeatId());
                                        }

                                        if (ticketPurchase.getEvent() != null) {
                                            ticketPurchaseDTO.setEventId(ticketPurchase.getEvent().getEventId());
                                        }
                                        orderDetailDTO.setTicketPurchaseDTO(ticketPurchaseDTO);
                                    }
                                    return orderDetailDTO;
                                })
                                .collect(Collectors.toList());
                        dto.setOrderDetail(orderDetailDTOs);
                    }
                    return dto;
                })
                .toList();
    }

    private String orderCodeAutomationCreating() {
        Account account = appUtils.getAccountFromAuthentication();
        int accountId = account.getAccountId(); // Giả định bạn có hàm lấy userId

        // Lấy thời gian hiện tại
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());

        // Tạo phần số thứ tự tự động hoặc ngẫu nhiên cho mã đơn hàng
        String uniqueId = String.format("%04d", new Random().nextInt(10000));

        return accountId + date  + uniqueId;
    }

    @Async
    public void scheduleStatusUpdate(LocalDateTime startTime, int orderId) {
        LocalDateTime currentTime = LocalDateTime.now();
        long delay = java.time.Duration.between(currentTime, startTime.plusMinutes(15)).toSeconds();

        if (delay > 0) {
            scheduler.schedule(() -> {
                Order order = orderRepository
                        .findById(orderId)
                        .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
                if (order.getStatus().equals(EOrderStatus.SUCCESSFUL.name())) {
                    return;
                }
                if (order.getStatus().equals(EOrderStatus.FAILURE.name())) {
                    return;
                }
                if (order.getStatus().equals(EOrderStatus.EXPIRED.name())) {
                    return;
                }
                if (order.getStatus().equals(EOrderStatus.PENDING.name())) {
                    List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
                    for (OrderDetail orderDetail : orderDetails) {
                        TicketPurchase ticketPurchase = ticketPurchaseRepository
                                .findById(orderDetail.getTicketPurchase().getTicketPurchaseId())
                                .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

                        Seat seat = seatRepository
                                .findById(ticketPurchase.getSeat().getSeatId())
                                .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));
                        seat.setStatus(true);

                        Zone zone = zoneRepository
                                .findById(ticketPurchase.getZone().getZoneId())
                                .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                        if (zone.getAvailableQuantity() < 1) {
                            zone.setAvailableQuantity(zone.getAvailableQuantity() + 1);
                            zone.setStatus(true);
                        } else {
                            zone.setAvailableQuantity(zone.getAvailableQuantity() + 1);
                        }
                        ticketPurchase.setStatus(ETicketPurchaseStatus.EXPIRED.name());

                        ticketPurchaseRepository.save(ticketPurchase);
                        seatRepository.save(seat);
                        zoneRepository.save(zone);
                    }
                    order.setStatus(EOrderStatus.EXPIRED.name());
                    orderRepository.save(order);
                }
            }, delay, java.util.concurrent.TimeUnit.SECONDS);
        } else {
            Order order = orderRepository
                    .findById(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
            if (order.getStatus().equals(EOrderStatus.SUCCESSFUL.name())) {
                return;
            }
            if (order.getStatus().equals(EOrderStatus.FAILURE.name())) {
                return;
            }
            if (order.getStatus().equals(EOrderStatus.EXPIRED.name())) {
                return;
            }
            if (order.getStatus().equals(EOrderStatus.PENDING.name())) {
                List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
                for (OrderDetail orderDetail : orderDetails) {
                    TicketPurchase ticketPurchase = ticketPurchaseRepository
                            .findById(orderDetail.getTicketPurchase().getTicketPurchaseId())
                            .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

                    Seat seat = seatRepository
                            .findById(ticketPurchase.getSeat().getSeatId())
                            .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));
                    seat.setStatus(true);

                    Zone zone = zoneRepository
                            .findById(ticketPurchase.getZone().getZoneId())
                            .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

                    if (zone.getAvailableQuantity() < 1) {
                        zone.setAvailableQuantity(zone.getAvailableQuantity() + 1);
                        zone.setStatus(true);
                    } else {
                        zone.setAvailableQuantity(zone.getAvailableQuantity() + 1);
                    }
                    ticketPurchase.setStatus(ETicketPurchaseStatus.EXPIRED.name());

                    ticketPurchaseRepository.save(ticketPurchase);
                    seatRepository.save(seat);
                    zoneRepository.save(zone);
                }
                order.setStatus(EOrderStatus.EXPIRED.name());
                orderRepository.save(order);
            }
        }
    }
}
