package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.SeatMapDTO;
import com.pse.tixclick.payload.request.create.CreateSeatMapRequest;
import com.pse.tixclick.payload.request.update.UpdateSeatMapRequest;
import com.pse.tixclick.payload.response.SectionResponse;

import java.util.List;

public interface SeatMapService {
    SeatMapDTO createSeatMap(CreateSeatMapRequest createSeatMapRequest);

    List<SeatMapDTO> getAllSeatMaps();

    SeatMapDTO getSeatMapByEventId(int eventId);

    void deleteSeatMap(int seatMapId);

    SeatMapDTO updateSeatMap(UpdateSeatMapRequest updateSeatMapRequest, int seatMapId);

    List<SectionResponse> designZone(List<SectionResponse> sectionResponse, int eventId);

}
