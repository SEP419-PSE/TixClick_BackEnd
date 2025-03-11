package com.pse.tixclick.payload.request.create;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.ticket.TicketPurchase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckinRequest {
    private int checkinId;

    private int ticketPurchaseId;
}
