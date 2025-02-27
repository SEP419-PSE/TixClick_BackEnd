package com.pse.tixclick.payload.dto;

import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.seatmap.Seat;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketPurchaseDTO {
    private int ticketPurchaseId;

    private String qrCode;

    private String status;

    private int zoneId;

    private int seatId;

    private int eventActivityId;

    private int ticketId;

    private int eventId;
}
