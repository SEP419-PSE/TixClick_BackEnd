package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyEventResponse {
    private String eventName;
    String url;
    List<MyEventActivityResponse> eventActivities;
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MyEventActivityResponse {
        int eventActivityId;
        private String eventActivityName;
        private LocalDate date;
        LocalTime startTime;
        LocalTime endTime;
        }
}
