package com.pse.tixclick.payload.request.update;

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
    String locationName;
    String status;
    String typeEvent;
    String description;
    int categoryId;
}
