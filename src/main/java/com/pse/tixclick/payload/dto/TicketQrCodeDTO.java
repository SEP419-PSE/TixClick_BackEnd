package com.pse.tixclick.payload.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
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
    private String ticket_code;
    private String zone_name;
    private String seat_row_number;
    private String seat_column_number;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date purchase_date;
    private String account_name;
    private String email;
    private String phone;
    private int checkin_Log_id;
}
