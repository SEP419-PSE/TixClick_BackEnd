package com.pse.tixclick.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.entity_enum.EPaymentStatus;
import com.pse.tixclick.payload.entity.payment.Order;
import com.pse.tixclick.payload.entity.payment.OrderDetail;
import com.pse.tixclick.payload.entity.payment.Payment;
import com.pse.tixclick.payload.entity.payment.Transaction;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import com.pse.tixclick.payload.response.PayOSResponse;
import com.pse.tixclick.payload.response.PaymentResponse;
import com.pse.tixclick.payment.PayOSUtils;
import com.pse.tixclick.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.mapper.Mapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private TicketPurchaseRepository ticketPurchaseRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private VoucherRepository voucherRepository;
    @Mock
    private ZoneActivityRepository zoneActivityRepository;
    @Mock
    private SeatActivityRepository seatActivityRepository;
    @Mock
    private CheckinLogRepository checkinLogRepository;
    @Mock
    private Mapper mapper;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        request = mock(HttpServletRequest.class);
    }

//    @Test
//    void testHandleCallbackPayOS_SuccessfulPayment() throws Exception {
//        // Mock request parameters
//        when(request.getParameter("status")).thenReturn("PAID");
//        when(request.getParameter("code")).thenReturn("00");
//        when(request.getParameter("orderId")).thenReturn("123");
//        when(request.getParameter("id")).thenReturn("456");
//        when(request.getParameter("userName")).thenReturn("testUser");
//        when(request.getParameter("orderCode")).thenReturn("ORD123");
//        when(request.getParameter("amount")).thenReturn("100.0");
//        when(request.getParameter("voucherCode")).thenReturn("VOUCHER123");
//
//        // Mock repositories
//        Payment payment = mock(Payment.class);
//        Order order = mock(Order.class);
//        Account account = mock(Account.class);
//        List<OrderDetail> orderDetails = Arrays.asList(mock(OrderDetail.class));
//
//        when(paymentRepository.findPaymentByOrderCode(anyString())).thenReturn(payment);
//        when(orderRepository.findById(anyInt())).thenReturn(Optional.of(order));
//        when(accountRepository.findAccountByUserName(anyString())).thenReturn(Optional.of(account));
//        when(orderDetailRepository.findByOrderId(anyInt())).thenReturn(orderDetails);
//        when(ticketPurchaseRepository.findById(anyInt())).thenReturn(Optional.of(mock(TicketPurchase.class)));
//
//        // Call method
//        PaymentResponse response = paymentService.handleCallbackPayOS(request);
//
//        // Verify interactions
//        verify(paymentRepository).save(payment);
//        verify(orderRepository).save(order);
//        verify(transactionRepository).save(any(Transaction.class));
//
//        // Assert the response status
//        assertEquals("PAID", response.getStatus());
//    }
//
//    @Test
//    void testHandleCallbackPayOS_FailedPayment() throws Exception {
//        // Mock request parameters
//        when(request.getParameter("status")).thenReturn("FAILED");
//        when(request.getParameter("code")).thenReturn("99");
//        when(request.getParameter("orderId")).thenReturn("123");
//        when(request.getParameter("id")).thenReturn("456");
//        when(request.getParameter("userName")).thenReturn("testUser");
//        when(request.getParameter("orderCode")).thenReturn("ORD123");
//        when(request.getParameter("amount")).thenReturn("100.0");
//        when(request.getParameter("voucherCode")).thenReturn("VOUCHER123");
//
//        // Mock repositories
//        Payment payment = mock(Payment.class);
//        Order order = mock(Order.class);
//        Account account = mock(Account.class);
//        List<OrderDetail> orderDetails = Arrays.asList(mock(OrderDetail.class));
//        Voucher voucher = mock(Voucher.class);
//
//        when(paymentRepository.findPaymentByOrderCode(anyString())).thenReturn(payment);
//        when(orderRepository.findById(anyInt())).thenReturn(Optional.of(order));
//        when(accountRepository.findAccountByUserName(anyString())).thenReturn(Optional.of(account));
//        when(orderDetailRepository.findByOrderId(anyInt())).thenReturn(orderDetails);
//        when(voucherRepository.existsByVoucherCode(anyString())).thenReturn(voucher);
//
//        // Call method
//        PaymentResponse response = paymentService.handleCallbackPayOS(request);
//
//        // Verify interactions
//        verify(paymentRepository).save(payment);
//        verify(orderRepository).save(order);
//        verify(voucherRepository).save(voucher);
//        verify(transactionRepository).save(any(Transaction.class));
//
//        // Assert the response status
//        assertEquals("FAILED", response.getStatus());
//    }
//
//    @Test
//    void testHandleCallbackPayOS_TransactionExists() throws Exception {
//        // Mock request parameters
//        when(request.getParameter("status")).thenReturn("PAID");
//        when(request.getParameter("code")).thenReturn("00");
//        when(request.getParameter("orderId")).thenReturn("123");
//        when(request.getParameter("id")).thenReturn("456");
//        when(request.getParameter("userName")).thenReturn("testUser");
//        when(request.getParameter("orderCode")).thenReturn("ORD123");
//        when(request.getParameter("amount")).thenReturn("100.0");
//        when(request.getParameter("voucherCode")).thenReturn("VOUCHER123");
//
//        // Mock repositories
//        Payment payment = mock(Payment.class);
//        Order order = mock(Order.class);
//        Account account = mock(Account.class);
//        Transaction existingTransaction = mock(Transaction.class);
//        List<OrderDetail> orderDetails = Arrays.asList(mock(OrderDetail.class));
//
//        when(paymentRepository.findPaymentByOrderCode(anyString())).thenReturn(payment);
//        when(orderRepository.findById(anyInt())).thenReturn(Optional.of(order));
//        when(accountRepository.findAccountByUserName(anyString())).thenReturn(Optional.of(account));
//        when(orderDetailRepository.findByOrderId(anyInt())).thenReturn(orderDetails);
//        when(transactionRepository.findByTransactionCode(anyString())).thenReturn(existingTransaction);
//
//        // Call method and verify exception
//        assertThrows(AppException.class, () -> paymentService.handleCallbackPayOS(request));
//
//        // Verify no transaction was saved
//        verify(transactionRepository, never()).save(any(Transaction.class));
//    }
}