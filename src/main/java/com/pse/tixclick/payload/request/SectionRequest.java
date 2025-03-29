package com.pse.tixclick.payload.request;

import com.pse.tixclick.payload.response.SeatResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SectionRequest {
    private String id;
    private String name;
    private int rows;
    private int columns;
    private List<SeatRequest> seats;
    private int x;
    private int y;
    private int width;
    private int height;
    private int capacity;
    private String type;
    private String priceId;

}
