package com.pse.tixclick.payload.request;

import com.pse.tixclick.payload.entity.entity_enum.ETypeEvent;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventRequest {
    int eventId;
    String eventName;
    String location;
    String status;
    String typeEvent;
    String description;
    int categoryId;
}
