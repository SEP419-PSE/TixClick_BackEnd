package com.pse.tixclick.payload.response;

import com.pse.tixclick.payload.dto.MyTicketDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyTicketResponse {
    int orderId;
    String orderCode;
    String orderDate;
    double totalPrice;
    double totalDiscount;
    List<MyTicketDTO> ticketPurchases;
}
