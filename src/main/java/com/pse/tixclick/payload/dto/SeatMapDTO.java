package com.pse.tixclick.payload.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.seatmap.Background;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatMapDTO {
    private int seatMapId;

    private String seatMapName;

    private String seatMapWidth;

    private String seatMapHeight;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean status;

    private BackgroundDTO background;

    private EventDTO event;
}
