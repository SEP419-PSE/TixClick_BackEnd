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
    String eventName;
    String location;
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
   List<EventActivityResponse> eventActivityDTOList;
   boolean isHaveSeatMap;
   double price;


}
