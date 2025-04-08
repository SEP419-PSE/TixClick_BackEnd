package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventDashboardResponse {
    int eventId;
    String eventName;
    String locationName;
    String location;
    String status;
    String typeEvent;
    int countView;
    String logoURL;
    String bannerURL;
    String description;
    String startDate;
    String endDate;
    String urlOnline;
    String eventCategory;
    Integer countTicketSold;
    Double totalRevenue;

    List<EventActivityResponse> eventActivityDTOList;

}
