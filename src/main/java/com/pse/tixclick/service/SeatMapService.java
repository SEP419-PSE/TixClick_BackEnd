package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.SeatMapDTO;
import com.pse.tixclick.payload.request.SeatRequest;
import com.pse.tixclick.payload.request.SectionRequest;
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

    List<SectionRequest> designZone(List<SectionRequest> sectionResponse, int eventId);

    List<SectionRequest> getSectionsByEventId(int eventId);
    List<SeatRequest> getSeatsByZoneId(int zoneId);

    List<SectionRequest> deleteZone(int zoneId);
}
