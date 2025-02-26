package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.SeatDTO;
import com.pse.tixclick.payload.request.create.CreateSeatRequest;
import com.pse.tixclick.payload.request.update.UpdateSeatRequest;

import java.util.List;

public interface SeatService {
    SeatDTO createSeat(CreateSeatRequest createSeatRequest);

    SeatDTO updateSeat(UpdateSeatRequest updateSeatRequest, int seatId);

    List<SeatDTO> getAllSeats();

    List<SeatDTO> getSeatsByZone(int zoneId);

    List<SeatDTO> getSeatsBySeatMap(int seatMapId);

    void deleteSeat(int seatId);
}
