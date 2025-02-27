package com.pse.tixclick.payload.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatDTO {
    private int seatId;

    private String rowNumber;

    private String columnNumber;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private boolean status;

    private int zoneId;
}
