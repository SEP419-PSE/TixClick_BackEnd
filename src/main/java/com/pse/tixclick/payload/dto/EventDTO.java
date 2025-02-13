package com.pse.tixclick.payload.dto;

import com.pse.tixclick.payload.entity.entity_enum.ETypeEvent;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventDTO {
    int eventId;
    String eventName;
    String location;
    String logoURL;
    String bannerURL;
    String logoOrganizerURL;
    String status;
    String typeEvent;
    String description;
    int categoryId;
    int organizerId;
}
