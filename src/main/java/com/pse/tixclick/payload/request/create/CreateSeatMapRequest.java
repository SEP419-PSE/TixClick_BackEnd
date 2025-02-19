package com.pse.tixclick.payload.request.create;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateSeatMapRequest {
    private String seatMapName;

    private String seatMapWidth;

    private String seatMapHeight;

    private int backgroundId;

    private int eventId;
}
