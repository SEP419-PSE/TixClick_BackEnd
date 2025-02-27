package com.pse.tixclick.payload.dto;



import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckInLogDTO {
    private int checkinId;

    private LocalDateTime checkinTime;

    private String checkinDevice;

    private String checkinLocation;

    private String checkinStatus;

    private int accountId;

    private int ticketPurchaseId;
}
