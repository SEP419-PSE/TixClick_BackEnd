package com.pse.tixclick.service;

import com.pse.tixclick.payload.response.PayOSResponse;
import com.pse.tixclick.payload.response.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService
{
    PayOSResponse changeOrderStatusPayOs(int orderId);
    PayOSResponse createPaymentLink(int orderId, HttpServletRequest request) throws Exception;

    PaymentResponse handleCallbackPayOS(HttpServletRequest request);
}
