package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardEventResponse {
    double totalRevenue;
    int countOrder;
    int countViewer;
    int countTicket;
}
