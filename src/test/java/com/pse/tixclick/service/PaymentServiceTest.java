package com.pse.tixclick.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.entity_enum.EPaymentStatus;
import com.pse.tixclick.payload.entity.payment.Order;
import com.pse.tixclick.payload.entity.payment.Payment;
import com.pse.tixclick.payload.response.PayOSResponse;
import com.pse.tixclick.payment.PayOSUtils;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.OrderRepository;
import com.pse.tixclick.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

//    @InjectMocks
//    private PaymentService paymentService;
//
//    @Mock
//    private OrderRepository orderRepository;
//
//    @Mock
//    private AccountRepository accountRepository;
//
//    @Mock
//    private PaymentRepository paymentRepository;
//
//    @Mock
//    private PayOSUtils payOSUtils;
//
//    @Mock
//    private PayOS payOS;
//
//    private ObjectMapper objectMapper;
//    private MockHttpServletRequest request;
//    private Order order;
//    private Account account;
//
//    @BeforeEach
//    void setUp() {
//        objectMapper = new ObjectMapper();
//        request = new MockHttpServletRequest();
//        request.setScheme("http");
//        request.setServerName("localhost");
//        request.setServerPort(8080);
//        request.setContextPath("/api");
//
//        account = new Account();
//        account.setAccountId(1); // Use Long for accountId
//        account.setUserName("testUser");
//
//        order = new Order();
//        order.setOrderId(1);
//        order.setOrderCode("123456");
//        order.setTotalAmountDiscount(1000.0);
//        order.setAccount(account);
//    }
//
//    @Test
//    void createPaymentLink_Success() throws Exception {
//        // Arrange
//        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
//        when(accountRepository.findById(1)).thenReturn(Optional.of(account)); // Use Long
//        when(paymentRepository.findByOrderId(1)).thenReturn(Collections.emptyList());
//        when(payOSUtils.payOS()).thenReturn(payOS);
//
//        CheckoutResponseData mockResponse = new CheckoutResponseData(); // PayOS SDK class
//        when(payOS.createPaymentLink(any())).thenReturn(mockResponse);
//        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // Act
//        PayOSResponse response = paymentService.createPaymentLink(1, "VOUCHER123", 3600, request);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals("ok", response.getError());
//        assertEquals("success", response.getMessage());
//        assertNotNull(response.getData());
//        verify(orderRepository, times(1)).findById(1);
//        verify(accountRepository, times(1)).findById(1);
//        verify(paymentRepository, times(1)).findByOrderId(1);
//        verify(paymentRepository, times(1)).save(any(Payment.class));
//        verify(payOSUtils, times(1)).payOS();
//        verify(payOS, times(1)).createPaymentLink(any());
//    }
//
//    @Test
//    void createPaymentLink_OrderNotFound_ThrowsAppException() {
//        // Arrange
//        when(orderRepository.findById(1)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        AppException exception = assertThrows(AppException.class, () ->
//                paymentService.createPaymentLink(1, "VOUCHER123", 3600, request));
//        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
//        verify(orderRepository, times(1)).findById(1);
//        verify(accountRepository, never()).findById(any());
//        verify(paymentRepository, never()).findByOrderId(anyInt());
//        verify(payOSUtils, never()).payOS();
//    }
//
//    @Test
//    void createPaymentLink_AccountNotFound_ThrowsAppException() {
//        // Arrange
//        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
//        when(accountRepository.findById(1)).thenReturn(Optional.empty()); // Use Long
//
//        // Act & Assert
//        AppException exception = assertThrows(AppException.class, () ->
//                paymentService.createPaymentLink(1, "VOUCHER123", 3600, request));
//        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
//        verify(orderRepository, times(1)).findById(1);
//        verify(accountRepository, times(1)).findById(1);
//        verify(paymentRepository, never()).findByOrderId(anyInt());
//        verify(payOSUtils, never()).payOS();
//    }
//
//    @Test
//    void createPaymentLink_PaymentAlreadyCompleted_ThrowsRuntimeException() {
//        // Arrange
//        Payment completedPayment = new Payment();
//        completedPayment.setStatus(EPaymentStatus.SUCCESSFULLY);
//        List<Payment> paymentHistory = List.of(completedPayment);
//
//        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
//        when(accountRepository.findById(1)).thenReturn(Optional.of(account)); // Use Long
//        when(paymentRepository.findByOrderId(1)).thenReturn(paymentHistory);
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class, () ->
//                paymentService.createPaymentLink(1, "VOUCHER123", 3600, request));
//        assertEquals("Payment is already completed", exception.getMessage());
//        verify(orderRepository, times(1)).findById(1);
//        verify(accountRepository, times(1)).findById(1);
//        verify(paymentRepository, times(1)).findByOrderId(1);
//        verify(payOSUtils, never()).payOS();
//    }
}