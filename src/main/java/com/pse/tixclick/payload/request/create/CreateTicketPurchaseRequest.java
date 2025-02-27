package com.pse.tixclick.payload.request.create;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTicketPurchaseRequest {
    private int zoneId;

    private int seatId;

    private int eventActivityId;

    private int ticketId;

    private int eventId;
}
