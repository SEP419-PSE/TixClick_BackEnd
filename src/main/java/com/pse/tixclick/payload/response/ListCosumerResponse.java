package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListCosumerResponse {
    String username;
    String email;
    String CCCD;
    String MSSV;
    String phone;

    List<TicketSheetResponse> ticketPurchases;
}
