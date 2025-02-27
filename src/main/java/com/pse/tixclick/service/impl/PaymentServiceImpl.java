package com.pse.tixclick.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.entity_enum.EOrderStatus;
import com.pse.tixclick.payload.entity.entity_enum.EPaymentStatus;
import com.pse.tixclick.payload.entity.payment.Order;
import com.pse.tixclick.payload.entity.payment.Payment;
import com.pse.tixclick.payload.entity.payment.Transaction;
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
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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
    public PaymentResponse handleCallbackPayOS(HttpServletRequest request) {
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
                    .orElseThrow(() -> new RuntimeException("Order not found!"));

            Account account = accountRepository
                    .findAccountByUserName(userName)
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

            order.setStatus(EOrderStatus.SUCCESSFUL.name());
            orderRepository.save(order);

            Transaction transaction = new Transaction();
            transaction.setAmount(Double.valueOf(amount));
            transaction.setTransactionCode(transactionNo);
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setDescription("Thanh toan don hang: " + orderCode);
            transaction.setAccount(account);
            transaction.setPayment(payment);
            transactionRepository.save(transaction);

            return new PaymentResponse(status, "SUCCESSFUL", mapper.map(payment, PaymentResponse.class));
        }
        else {
            payment.setStatus(EPaymentStatus.FAILURE.name());
            paymentRepository.save(payment);

            Order order = orderRepository
                    .findById(Integer.valueOf(orderId))
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
            order.setStatus(EOrderStatus.CANCELLED.name());
            orderRepository.save(order);

            return new PaymentResponse(status, "CANCELLED", mapper.map(payment, PaymentResponse.class));
        }
    }


    private String getBaseUrl(HttpServletRequest request) {
        // Trả về URL cố định
        return "https://tixclick.site/";
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
