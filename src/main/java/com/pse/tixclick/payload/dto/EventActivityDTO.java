package com.pse.tixclick.payload.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventActivityDTO {

    int Id;
    String activityName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date;

    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime startTime;

    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime endTime;

    int eventId;
    int seatmapId;
    int createdBy;
}
