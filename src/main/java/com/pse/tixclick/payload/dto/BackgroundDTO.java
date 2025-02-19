package com.pse.tixclick.payload.dto;

import com.pse.tixclick.payload.entity.seatmap.SeatMap;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BackgroundDTO {
    private int backgroundId;

    private String backgroundName;

    private String Type;

    private String value;
}
