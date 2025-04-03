package com.pse.tixclick.payload.dto;

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
public class EventDTO {
    int eventId;
    String eventName;
    String location;
    String locationName;
    int countView;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate;
    String logoURL;
    String bannerURL;
    String status;
    String typeEvent;
    String description;
    int categoryId;
    int organizerId;
    int companyId;
}
