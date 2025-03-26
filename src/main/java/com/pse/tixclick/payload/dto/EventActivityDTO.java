package com.pse.tixclick.payload.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventActivityDTO {
    int eventActivityId;

    String activityName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dateEvent;

    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime startTimeEvent;

    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime endTimeEvent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime startTicketSale;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime endTicketSale;

    int seatMapId;

    int eventId;

    int createdBy;
}
