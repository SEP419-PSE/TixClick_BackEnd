package com.pse.tixclick.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.dto.EventActivityDTO;
import com.pse.tixclick.payload.dto.TicketDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class   CreateEventActivityAndTicketRequest {

    String activityName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dateEvent;

    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime startTimeEvent;

    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime endTimeEvent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime startTicketSale;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime endTicketSale;

    int eventId;

    List<TicketRequest> tickets;

    // Inner class TicketRequest
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TicketRequest {
        String ticketName;
        String ticketCode;
        int quantity;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdDate;
        double price;
        int minQuantity;
        int maxQuantity;
        boolean status;
        int eventId;
    }


}
