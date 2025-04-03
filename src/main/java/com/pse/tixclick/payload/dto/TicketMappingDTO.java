package com.pse.tixclick.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketMappingDTO {
    int ticketMappingId;
    int ticketId;
    int eventActivityId;
    int quantity;
    boolean status;
}