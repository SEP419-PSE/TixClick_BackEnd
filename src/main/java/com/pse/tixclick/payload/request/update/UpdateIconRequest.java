package com.pse.tixclick.payload.request.update;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateIconRequest {
    private String iconName;

    private String xPosition;

    private String yPosition;

    private String url;

    private int seatMapId;
}
