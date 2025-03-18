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
    private String eventName;
    private LocalDate eventDate;
    private LocalTime eventStartTime;
    private String location;
    private double price;
    private String seatCode;
    private String ticketType;
}
