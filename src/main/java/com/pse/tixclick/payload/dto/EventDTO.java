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
    String eventCode;
    String city;
    String district;
    String ward;
    String locationName;
    int countView;
    String logoURL;
    String bannerURL;
    String urlOnline;
    String status;
    String typeEvent;
    String description;
    int categoryId;
    int organizerId;
    int companyId;
}
