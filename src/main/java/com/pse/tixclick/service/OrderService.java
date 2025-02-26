package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.OrderDTO;
import com.pse.tixclick.payload.request.create.CreateOrderRequest;

public interface OrderService {
    OrderDTO createOrder(CreateOrderRequest createOrderRequest);

}
