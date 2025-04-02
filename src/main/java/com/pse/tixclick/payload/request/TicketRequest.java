package com.pse.tixclick.payload.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketRequest {
    int ticketId;
    String id;
    String name;
    String color;
    String textColor;
    double price;
    int maxQuantity;
}
