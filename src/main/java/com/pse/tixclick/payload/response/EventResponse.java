package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventResponse {
    int eventId;
    String eventName;
    String locationName;
    String description;
    String bannerURL;
    String logoURL;
    String eventCode;
    String city;
    String district;
    String ward;
    String address;
    String status;
    String typeEvent;
    int organizerId;
    String organizerName;
    int companyId;
    String companyName;
}
