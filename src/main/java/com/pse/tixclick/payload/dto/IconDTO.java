package com.pse.tixclick.payload.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.seatmap.SeatMap;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IconDTO {
    private int iconId;

    private String iconName;

    private String xPosition;

    private String yPosition;

    private String url;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private boolean status;

    private int seatMapId;
}
