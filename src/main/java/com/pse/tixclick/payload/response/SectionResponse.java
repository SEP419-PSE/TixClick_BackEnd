package com.pse.tixclick.payload.response;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionResponse {
    private int idZone;
    private String name;
    private int rows;
    private int columns;
    private List<SeatResponse> seats;
    private int x;
    private int y;
    private int width;
    private int height;
    private int zoneTypeId;
}
