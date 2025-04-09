package com.pse.tixclick.payload.request.create;

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
public class CreateEventRequest {
    String eventName;
    String location;
    String typeEvent;
    String description;
    String urlOnline;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate;
    String locationName;
    int categoryId;
    int companyId;
}
