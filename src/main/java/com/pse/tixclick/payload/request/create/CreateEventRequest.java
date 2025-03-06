package com.pse.tixclick.payload.request.create;

import com.pse.tixclick.payload.entity.entity_enum.ETypeEvent;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateEventRequest {
    String eventName;
    String location;
    String typeEvent;
    String description;
    String locationName;
    int categoryId;
    int companyId;
}
