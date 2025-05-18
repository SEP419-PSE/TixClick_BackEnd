package com.pse.tixclick.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pse.tixclick.payload.request.create.CreateTicketPurchaseRequest;

import java.util.List;

public class ChangeTicketRequest {
    @JsonProperty("ticketPurchaseRequests")
    private List<TicketPurchaseRequest> ticketPurchaseRequests;

    @JsonProperty("ticketChange")
    private List<CreateTicketPurchaseRequest> ticketChange;

    @JsonProperty("orderCode")
    private String orderCode;

    // Constructor
    public ChangeTicketRequest() {
    }

    // Getters and setters
    public List<TicketPurchaseRequest> getTicketPurchaseRequests() {
        return ticketPurchaseRequests;
    }

    public void setTicketPurchaseRequests(List<TicketPurchaseRequest> ticketPurchaseRequests) {
        this.ticketPurchaseRequests = ticketPurchaseRequests;
    }

    public List<CreateTicketPurchaseRequest> getTicketChange() {
        return ticketChange;
    }

    public void setTicketChange(List<CreateTicketPurchaseRequest> ticketChange) {
        this.ticketChange = ticketChange;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }
}