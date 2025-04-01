package com.pse.tixclick.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetSeatResponse {
    private int seatActivityId;
    private String id;

    private String row;

    private String column;

    private String seatTypeId;

    private boolean status;
}
