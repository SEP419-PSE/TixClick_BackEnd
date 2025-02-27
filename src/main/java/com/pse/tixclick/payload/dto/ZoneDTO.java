package com.pse.tixclick.payload.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.seatmap.SeatMap;
import com.pse.tixclick.payload.entity.seatmap.ZoneType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZoneDTO {
    private int zoneId;

    private String zoneName;

    private String xPosition;

    private String yPosition;

    private String width;

    private String height;

    private int quantity;

    private int availableQuantity;

    private String rows;

    private String columns;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private boolean status;

    private int seatMapId;

    private int zoneTypeId;
}
