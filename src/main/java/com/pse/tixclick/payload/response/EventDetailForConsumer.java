package com.pse.tixclick.payload.response;

import com.pse.tixclick.payload.dto.EventActivityDTO;
import com.pse.tixclick.payload.dto.TicketDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventDetailForConsumer {
    int eventId;
    String eventName;
    String city;
    String district;
    String ward;
    String locationName;
    String logoURL;
    String bannerURL;
    String companyURL;
    String companyName;
    String descriptionCompany;
    String status;
    String typeEvent;
    String description;
    String eventCategory;
    int eventCategoryId;
   List<EventActivityResponse> eventActivityDTOList;
   boolean isHaveSeatMap;
   double price;


}
