package com.pse.tixclick.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketQrCodeDTO {
    private String event_name;
    private String activity_name;
    private String ticket_name;
    private String zone_name;
    private String seat_row_number;
    private String seat_column_number;
    private String account_name;
    private Date purchase_date;
    private String phone;
}
