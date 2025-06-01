package com.pse.tixclick.payload.response;

public interface TicketCheckinStatsProjection {
    String getTicketName();
    Integer getTicketId();
    Integer getTotalPurchased();
    Integer getCheckedIn();
    Integer getTotalTicket();
}
