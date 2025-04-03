package com.pse.tixclick.payload.response;

import com.pse.tixclick.payload.dto.TicketDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketMappingResponse {
    int id;
    TicketDTO ticket;
    int quantity;
    boolean status;
}
