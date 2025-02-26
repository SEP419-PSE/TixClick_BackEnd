package com.pse.tixclick.service.impl;

import com.pse.tixclick.payload.dto.OrderDTO;
import com.pse.tixclick.payload.request.create.CreateOrderRequest;
import com.pse.tixclick.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class OrderServiceImpl implements OrderService {

    @Override
    public OrderDTO createOrder(CreateOrderRequest createOrderRequest) {
        return null;
    }
}
