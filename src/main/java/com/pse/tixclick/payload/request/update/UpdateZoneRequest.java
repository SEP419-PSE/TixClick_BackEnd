package com.pse.tixclick.payload.request.update;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateZoneRequest {
    private String zoneName;

    private String xPosition;

    private String yPosition;

    private String width;

    private String height;

    private int quantity;

    private int availableQuantity;

    private String rows;

    private String columns;

    private int zoneTypeId;
}
