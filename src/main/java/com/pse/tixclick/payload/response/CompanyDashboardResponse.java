package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyDashboardResponse {
    List<EventActivityDashbroadResponse> eventActivityDashbroadResponseList;
    List<TicketReVenueDashBoardResponse> ticketReVenueDashBoardResponseList;
    List<EventActivityRevenueReportResponse> eventActivityRevenueReportResponseList;
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TicketReVenueDashBoardResponse {
        String type;
        double value;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class EventActivityRevenueReportResponse {
        String date;
        double revenue;
    }

}
