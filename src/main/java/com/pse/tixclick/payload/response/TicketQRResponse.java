package com.pse.tixclick.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketQRResponse {
    private String eventName;
    private LocalDate eventDate;
    private LocalTime startTime;
    private String orderCode;
    private String userName;
    private String fullName;
    private String checkinStatus;
    private List<TicketDetailResponse> ticketDetails;

    public class TicketDetailResponse{
        private int ticketPurchaseId;
        private double price;
        private String seatCode;
        private String seatName;
        private String ticketType;
        private String zoneName;
        private int quantity;
        private String orderCode;
    }


}
