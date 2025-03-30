package com.pse.tixclick.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pse.tixclick.payload.request.SeatRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SectionResponse {
    private String id;
    private String name;
    private int rows;
    private int columns;
    private List<SeatResponse> seats;
    private int x;
    private int y;
    private int width;
    private int height;
    private int capacity;
    private String type;
    private String priceId;
    private double price;
    @JsonProperty("isSave")
    private boolean isSave;

}
