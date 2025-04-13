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
    String eventCode;
    String locationName;
    String city;
    String district;
    String ward;
    String status;
    String typeEvent;
    int countView;
    String logoURL;
    String bannerURL;
    String description;
    String urlOnline;
    String eventCategory;
    Integer countTicketSold;
    Double totalRevenue;
    boolean haveSeatMap;

    List<EventActivityResponse> eventActivityDTOList;

}
