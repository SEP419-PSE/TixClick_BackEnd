package com.pse.tixclick.payload.request.update;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateSeatRequest {
    private String rowNumber;

    private String columnNumber;

    private int zoneId;
}
