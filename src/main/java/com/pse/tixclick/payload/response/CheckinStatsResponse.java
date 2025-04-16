package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckinStatsResponse {
    int totalCheckin;
    int checkedIn;
    int notCheckedIn;
}
