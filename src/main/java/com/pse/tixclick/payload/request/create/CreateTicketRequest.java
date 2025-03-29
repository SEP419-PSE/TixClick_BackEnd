package com.pse.tixclick.payload.request.create;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTicketRequest {
    String ticketName;
    String ticketCode;
    int quantity;
    double price;
    int minQuantity;
    int maxQuantity;
    int eventId;
    boolean status;
    String textColor;
    String seatBackgroundColor;

}
