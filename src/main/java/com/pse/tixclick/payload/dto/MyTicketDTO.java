package com.pse.tixclick.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyTicketDTO {
    private int eventId;
    private int eventActivityId;
    private int ticketPurchaseId;
    private int eventCategoryId;
    private String eventName;
    private LocalDate eventDate;
    private LocalTime eventStartTime;
    private String timeBuyTicket;
    private String locationName;
    private String location;
    private double price;
    private String seatCode;
    private String ticketType;
    private String qrCode;
    private String zoneName;
    private int quantity;
    boolean ishaveSeatmap;
    String logo;
    String banner;
}
