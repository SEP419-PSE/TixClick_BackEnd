package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventActivityDashbroadResponse {
    String eventActivity;
    List<TicketDashBoardResponse> ticketDashBoardResponseList;
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TicketDashBoardResponse {
        String name;
        int value;
    }
}
