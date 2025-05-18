package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketOwnerResponse {



    private int ticketPurchaseId;
    private double price;
    private String seatCode;
    private String ticketType;
    private String zoneName;
    private int quantity;
    private String orderCode;
}
