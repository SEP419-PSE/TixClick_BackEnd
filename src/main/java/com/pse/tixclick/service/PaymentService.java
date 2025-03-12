package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.PaymentDTO;
import com.pse.tixclick.payload.response.PayOSResponse;
import com.pse.tixclick.payload.response.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface PaymentService
{
    PayOSResponse changeOrderStatusPayOs(int orderId);

    PayOSResponse createPaymentLink(int orderId, HttpServletRequest request) throws Exception;

    PaymentResponse handleCallbackPayOS(HttpServletRequest request);

    List<PaymentDTO> getAllPayments();
}
