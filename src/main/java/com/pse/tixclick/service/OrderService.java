package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.OrderDTO;
import com.pse.tixclick.payload.dto.Order_OrderDetailDTO;
import com.pse.tixclick.payload.request.create.CreateOrderRequest;

import java.util.List;

public interface OrderService {
    OrderDTO createOrder(CreateOrderRequest createOrderRequest);

    List<Order_OrderDetailDTO> getAllOrderOfUser();

    List<Order_OrderDetailDTO> getOrderStatusOfUser(String status);

}
