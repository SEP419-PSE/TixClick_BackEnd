package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventForConsumerResponse {
    String bannerURL;
    int eventId;
    String logoURL;
}
