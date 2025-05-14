package com.pse.tixclick.payload.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
public class MyTicketFlatDTO {

    private int orderId;
    private String orderCode;
    private LocalDateTime orderDate;
    private double totalAmount;
    private double totalAmountDiscount;

    private int ticketPurchaseId;
    private String qrCode;
    private int quantity;  // Lưu lại số lượng vé trong từng ticket purchase
    private double price;
    private String ticketName;

    private int eventId;
    private String eventName;
    private String logo;
    private String banner;
    private String locationName;
    private int eventCategoryId;

    private String address;
    private String ward;
    private String district;
    private String city;
    private Boolean hasSeatMap;

    private Integer eventActivityId;
    private LocalDate eventDate;
    private LocalTime eventStartTime;

    private String zoneName;
    private String seatCode;
}
