package com.pse.tixclick.payload.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
public class MyTicketFlatDTO {
    int orderId;
    String orderCode;
    LocalDateTime orderDate;
    double totalPrice;
    double totalDiscount;

    int ticketPurchaseId;
    String qrCode;
    int quantity;
    double price;
    String ticketType;

    int eventId;
    String eventName;
    String logo;
    String banner;
    String locationName;
    int eventCategoryId;

    String address;
    String ward;
    String district;
    String city;
    Boolean ishaveSeatmap;

    Integer eventActivityId;
    LocalDate eventDate;
    LocalTime eventStartTime;

    String zoneName;
    String seatCode;
}
