package com.pse.tixclick.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatResponse {
    private int seatId;
    private String seatName;

    private String rowNumber;

    private String columnNumber;

    private LocalDateTime createdDate;

    private boolean status;

    private String ticketId;

}
