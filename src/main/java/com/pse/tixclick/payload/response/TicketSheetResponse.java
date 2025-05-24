package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketSheetResponse {
    private String orderCode;
    private int orderId;
    boolean isHaveCheckin;
    List<TicketPurchaseResponse> ticketPurchases;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TicketPurchaseResponse {
        private int ticketPurchaseId;
        private double price;
        private String seatCode;
        private String ticketType;
        private String zoneName;
        private int quantity;
    }
}

