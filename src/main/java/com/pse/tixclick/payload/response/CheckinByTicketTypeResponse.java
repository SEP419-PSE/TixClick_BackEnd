package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckinByTicketTypeResponse {
    int eventActivityId;
    List<TicketTypeCheckinStat> checkinStats;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TicketTypeCheckinStat {
        String ticketType;
        int checkedIn;
        int total;
        double percentage;
    }
}