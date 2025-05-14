package com.pse.tixclick.payload.response;

import com.pse.tixclick.payload.dto.MyTicketDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;
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
    private int eventId;
    private int eventActivityId;
    private int eventCategoryId;
    private String eventName;
    private LocalDate eventDate;
    private LocalTime eventStartTime;
    private String timeBuyOrder;
    private String locationName;
    private String location;
    private String qrCode;
    String logo;
    String banner;
    List<TicketOwnerResponse> ticketPurchases;
}
