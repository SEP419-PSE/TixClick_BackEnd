package com.pse.tixclick.payload.request.create;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateSeatRequest {
    private String rowNumber;

    private String columnNumber;

    private int zoneId;
}
