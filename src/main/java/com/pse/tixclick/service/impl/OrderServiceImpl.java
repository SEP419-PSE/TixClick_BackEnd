package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.*;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.entity_enum.EOrderStatus;
import com.pse.tixclick.payload.entity.entity_enum.ETicketPurchaseStatus;
import com.pse.tixclick.payload.entity.entity_enum.EVoucherStatus;
import com.pse.tixclick.payload.entity.payment.Order;
import com.pse.tixclick.payload.entity.payment.OrderDetail;
import com.pse.tixclick.payload.entity.payment.Voucher;
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
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;


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
    TicketPurchaseRepository ticketPurchaseRepository;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    VoucherRepository voucherRepository;



    @Override
    public OrderDTO createOrder(CreateOrderRequest createOrderRequest) {
        Order order = new Order();
        order.setOrderCode(orderCodeAutomationCreating());
        order.setStatus(EOrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setAccount(appUtils.getAccountFromAuthentication());
        order.setTotalAmount(0);
        order = orderRepository.save(order);

        double totalAmount = 0;
        double newTotalAmount1 = 0;
        Set<Integer> ticketPurchaseIds = new HashSet<>();
        int quantity;

        for (TicketOrderDTO ticketOrderDTO : createOrderRequest.getTicketOrderDTOS()) {
            int ticketPurchaseId = ticketOrderDTO.getTicketPurchaseId();

            if (!ticketPurchaseIds.add(ticketPurchaseId)) {
                throw new AppException(ErrorCode.DUPLICATE_TICKET_PURCHASE);
            }

            TicketPurchase ticketPurchase = ticketPurchaseRepository
                    .findById(ticketPurchaseId)
                    .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));
            if(ticketPurchase.getStatus().equals(ETicketPurchaseStatus.EXPIRED)) {
                throw new AppException(ErrorCode.TICKET_PURCHASE_EXPIRED);
            }
            if(ticketPurchase.getStatus().equals(ETicketPurchaseStatus.CHECKED_IN)) {
                throw new AppException(ErrorCode.TICKET_PURCHASE_CHECKED_IN);
            }
            if (ticketPurchase.getStatus().equals(ETicketPurchaseStatus.CANCELLED)) {
                throw new AppException(ErrorCode.TICKET_PURCHASE_CANCELLED);
            }

            quantity = ticketPurchase.getQuantity();

            Ticket ticket = ticketRepository.findById(ticketPurchase.getTicket().getTicketId())
                    .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setTicketPurchase(ticketPurchase);

            double amount = ticket.getPrice() * quantity;

            orderDetail.setAmount(amount);
            totalAmount += amount;

            orderDetailRepository.save(orderDetail);
        }

        order.setTotalAmount(totalAmount);

        TicketPurchase ticketPurchase = ticketPurchaseRepository
                .findById(ticketPurchaseIds.iterator().next())
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

        if (createOrderRequest.getVoucherCode() != null && !createOrderRequest.getVoucherCode().isEmpty()) {
            Voucher voucher = voucherRepository.findByVoucherCodeAndEvent(createOrderRequest.getVoucherCode(), ticketPurchase.getEvent().getEventId())
                    .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
            double newTotalAmount = totalAmount - (totalAmount * voucher.getDiscount() / 100);
            newTotalAmount1 = newTotalAmount;
            order.setTotalAmountDiscount(newTotalAmount);
            voucher.setQuantity(voucher.getQuantity() - 1);
            if(voucher.getQuantity() == 0) {
                voucher.setStatus(EVoucherStatus.INACTIVE);
            }
            voucherRepository.save(voucher);
        }else {
            newTotalAmount1 = totalAmount;
            order.setTotalAmountDiscount(totalAmount);
        }
        orderRepository.save(order);

        OrderDTO orderDTO = mapper.map(order, OrderDTO.class);
        orderDTO.setOrderId(order.getOrderId());
        orderDTO.setTotalAmount(newTotalAmount1);
        orderDTO.setVoucherCode(createOrderRequest.getVoucherCode());
        orderDTO.setStatus(order.getStatus().toString());
        orderDTO.setAccountId(order.getAccount().getAccountId());
        orderDTO.setOrderCode(order.getOrderCode());
        return orderDTO;
    }

    @Override
    public List<Order_OrderDetailDTO> getAllOrderOfUser() {
//        Account account = appUtils.getAccountFromAuthentication();
//        List<Order> orders = orderRepository.findByAccountId(account.getAccountId());
//
//        return orders.stream()
//                .map(order -> {
//                    Order_OrderDetailDTO dto = mapper.map(order, Order_OrderDetailDTO.class);
//
//                    List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getOrderId());
//                    if (!orderDetails.isEmpty()) {
//                        List<OrderDetailDTO> orderDetailDTOs = orderDetails.stream()
//                                .map(orderDetail -> {
//                                    OrderDetailDTO orderDetailDTO = mapper.map(orderDetail, OrderDetailDTO.class);
//
//                                    if (orderDetail.getTicketPurchase() != null) {
//                                        TicketPurchase ticketPurchase = orderDetail.getTicketPurchase();
//                                        TicketPurchaseDTO ticketPurchaseDTO = new TicketPurchaseDTO();
//
//                                        ticketPurchaseDTO.setTicketPurchaseId(ticketPurchase.getTicketPurchaseId());
//                                        ticketPurchaseDTO.setQrCode(ticketPurchase.getQrCode());
//                                        ticketPurchaseDTO.setStatus(ticketPurchase.getStatus());
//
//                                        if (ticketPurchase.getTicket() != null) {
//                                            ticketPurchaseDTO.setTicketId(ticketPurchase.getTicket().getTicketId());
//                                        }
//
//                                        if (ticketPurchase.getEventActivity() != null) {
//                                            ticketPurchaseDTO.setEventActivityId(ticketPurchase.getEventActivity().getEventActivityId());
//                                        }
//
//                                        if (ticketPurchase.getZone() != null) {
//                                            ticketPurchaseDTO.setZoneId(ticketPurchase.getZone().getZoneId());
//                                        }
//
//                                        if (ticketPurchase.getSeat() != null) {
//                                            ticketPurchaseDTO.setSeatId(ticketPurchase.getSeat().getSeatId());
//                                        }
//
//                                        if (ticketPurchase.getEvent() != null) {
//                                            ticketPurchaseDTO.setEventId(ticketPurchase.getEvent().getEventId());
//                                        }
//                                        orderDetailDTO.setTicketPurchaseDTO(ticketPurchaseDTO);
//                                    }
//                                    return orderDetailDTO;
//                                })
//                                .collect(Collectors.toList());
//                        dto.setOrderDetail(orderDetailDTOs);
//                    }
//                    return dto;
//                })
//                .toList();
        return null;
    }

    @Override
    public List<Order_OrderDetailDTO> getOrderStatusOfUser(String status) {
//        Account account = appUtils.getAccountFromAuthentication();
//        List<Order> orders = orderRepository.findByStatusOfAccount(account.getAccountId(), status);
//
//        return orders.stream()
//                .map(order -> {
//                    Order_OrderDetailDTO dto = mapper.map(order, Order_OrderDetailDTO.class);
//
//                    List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getOrderId());
//                    if (!orderDetails.isEmpty()) {
//                        List<OrderDetailDTO> orderDetailDTOs = orderDetails.stream()
//                                .map(orderDetail -> {
//                                    OrderDetailDTO orderDetailDTO = mapper.map(orderDetail, OrderDetailDTO.class);
//
//                                    if (orderDetail.getTicketPurchase() != null) {
//                                        TicketPurchase ticketPurchase = orderDetail.getTicketPurchase();
//                                        TicketPurchaseDTO ticketPurchaseDTO = new TicketPurchaseDTO();
//
//                                        ticketPurchaseDTO.setTicketPurchaseId(ticketPurchase.getTicketPurchaseId());
//                                        ticketPurchaseDTO.setQrCode(ticketPurchase.getQrCode());
//                                        ticketPurchaseDTO.setStatus(ticketPurchase.getStatus());
//
//                                        if (ticketPurchase.getTicket() != null) {
//                                            ticketPurchaseDTO.setTicketId(ticketPurchase.getTicket().getTicketId());
//                                        }
//
//                                        if (ticketPurchase.getEventActivity() != null) {
//                                            ticketPurchaseDTO.setEventActivityId(ticketPurchase.getEventActivity().getEventActivityId());
//                                        }
//
//                                        if (ticketPurchase.getZone() != null) {
//                                            ticketPurchaseDTO.setZoneId(ticketPurchase.getZone().getZoneId());
//                                        }
//
//                                        if (ticketPurchase.getSeat() != null) {
//                                            ticketPurchaseDTO.setSeatId(ticketPurchase.getSeat().getSeatId());
//                                        }
//
//                                        if (ticketPurchase.getEvent() != null) {
//                                            ticketPurchaseDTO.setEventId(ticketPurchase.getEvent().getEventId());
//                                        }
//                                        orderDetailDTO.setTicketPurchaseDTO(ticketPurchaseDTO);
//                                    }
//                                    return orderDetailDTO;
//                                })
//                                .collect(Collectors.toList());
//                        dto.setOrderDetail(orderDetailDTOs);
//                    }
//                    return dto;
//                })
//                .toList();
        return null;
    }

    public String orderCodeAutomationCreating() {
        Account account = appUtils.getAccountFromAuthentication();
        int accountId = account.getAccountId(); // Giả định bạn có hàm lấy userId

        // Lấy thời gian hiện tại
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());

        // Tạo phần số thứ tự tự động hoặc ngẫu nhiên cho mã đơn hàng
        String uniqueId = String.format("%04d", new Random().nextInt(10000));

        return accountId + date  + uniqueId;
    }
}
