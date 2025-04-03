package com.pse.tixclick.payload.request.update;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pse.tixclick.payload.entity.entity_enum.ETypeEvent;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

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
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate;
    String status;
    String typeEvent;
    String description;
    int categoryId;
}
