package com.pse.tixclick.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeatRequest {
    private String id;

    private String row;

    private String column;

    private boolean status;

    private String seatTypeId;

}
