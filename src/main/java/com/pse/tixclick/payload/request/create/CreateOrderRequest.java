package com.pse.tixclick.payload.request.create;

import com.pse.tixclick.payload.dto.TicketOrderDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateOrderRequest {
    private List<TicketOrderDTO> ticketOrderDTOS;

    private String voucherCode;
}
