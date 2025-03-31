package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.SeatMapDTO;
import com.pse.tixclick.payload.request.SeatRequest;
import com.pse.tixclick.payload.request.SectionRequest;
import com.pse.tixclick.payload.request.create.CreateSeatMapRequest;
import com.pse.tixclick.payload.request.update.UpdateSeatMapRequest;
import com.pse.tixclick.payload.response.SeatResponse;
import com.pse.tixclick.payload.response.SectionResponse;

import java.util.List;

public interface SeatMapService {
    SeatMapDTO createSeatMap(CreateSeatMapRequest createSeatMapRequest);

    List<SeatMapDTO> getAllSeatMaps();

    SeatMapDTO getSeatMapByEventId(int eventId);

    void deleteSeatMap(int seatMapId);

    SeatMapDTO updateSeatMap(UpdateSeatMapRequest updateSeatMapRequest, int seatMapId);

    List<SectionResponse> designZone(List<SectionRequest> sectionResponse, int eventId);

    List<SectionResponse> getSectionsByEventId(int eventId);
    List<SeatResponse> getSeatsByZoneId(int zoneId);

    List<SectionResponse> deleteZone(List<SectionRequest> sectionResponse,String zoneId, int eventId);

    List<SectionResponse> getSections(int eventId, int eventActivityId);
}
