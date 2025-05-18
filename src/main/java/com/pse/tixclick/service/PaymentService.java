package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.PaymentDTO;
import com.pse.tixclick.payload.dto.TicketQrCodeDTO;
import com.pse.tixclick.payload.request.TicketPurchaseRequest;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;
import com.pse.tixclick.payload.response.PayOSResponse;
import com.pse.tixclick.payload.response.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface PaymentService
{
    PayOSResponse changeOrderStatusPayOs(int orderId);

    PayOSResponse createPaymentLink(int orderId, String voucherCode, long expiredTime, HttpServletRequest request) throws Exception;

    PayOSResponse changTicket(List<TicketPurchaseRequest> ticketPurchaseRequests, List<CreateTicketPurchaseRequest> ticketChange, String orderCode, HttpServletRequest request) throws Exception;
    PaymentResponse handleCallbackPayOS(HttpServletRequest request) throws Exception;

    List<PaymentDTO> getAllPayments();

    String testQR(TicketQrCodeDTO ticketQrCodeDTO) throws Exception;

    void exportRefunds(List<String> columns, OutputStream os, int eventId) throws IOException;

    String readOrderCodeAndStatus(InputStream is) throws IOException;
}
