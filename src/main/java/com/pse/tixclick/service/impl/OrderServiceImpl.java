package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.OrderDTO;
import com.pse.tixclick.payload.dto.TicketOrderDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.entity_enum.EOrderStatus;
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
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

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
    AccountRepository accountRepository;

    @Autowired
    OrderDetailRepository orderDetailRepository;

    @Autowired
    ModelMapper mapper;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    TicketPurchaseRepository ticketPurchaseRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    VoucherRepository voucherRepository;




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

        for (TicketOrderDTO ticketOrderDTO : createOrderRequest.getTicketOrderDTOS()) {
            int ticketPurchaseId = ticketOrderDTO.getTicketPurchaseId();
            int quantity = ticketOrderDTO.getQuantity();

            TicketPurchase ticketPurchase = ticketPurchaseRepository
                    .findById(ticketPurchaseId)
                    .orElseThrow(() -> new AppException(ErrorCode.TICKET_PURCHASE_NOT_FOUND));

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
        return mapper.map(order, OrderDTO.class);
    }


    private String orderCodeAutomationCreating() {
        Account account = appUtils.getAccountFromAuthentication();
        int accountId = account.getAccountId(); // Giả định bạn có hàm lấy userId

        // Lấy thời gian hiện tại
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());

        // Tạo phần số thứ tự tự động hoặc ngẫu nhiên cho mã đơn hàng
        // Bạn có thể thay thế bằng logic lấy số thứ tự từ DB
        String uniqueId = String.format("%04d", new Random().nextInt(10000));

        return accountId + date  + uniqueId;
    }


}
