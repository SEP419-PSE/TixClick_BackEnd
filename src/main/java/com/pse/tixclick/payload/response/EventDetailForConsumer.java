package com.pse.tixclick.payload.response;

import com.pse.tixclick.payload.dto.EventActivityDTO;
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
    String status;
    String typeEvent;
    String description;
    String categoryId;
    int organizerId;
   List<EventActivityDTO> eventActivityDTOList;
   boolean isHaveSeatMap;
   double price;

}
