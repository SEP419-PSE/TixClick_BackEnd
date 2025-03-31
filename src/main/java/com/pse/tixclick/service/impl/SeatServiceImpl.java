package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.SeatDTO;
import com.pse.tixclick.payload.entity.seatmap.Seat;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import com.pse.tixclick.payload.request.create.CreateSeatRequest;
import com.pse.tixclick.payload.request.update.UpdateSeatRequest;
import com.pse.tixclick.repository.SeatMapRepository;
import com.pse.tixclick.repository.SeatRepository;
import com.pse.tixclick.repository.ZoneRepository;
import com.pse.tixclick.repository.ZoneTypeRepository;
import com.pse.tixclick.service.SeatService;
import com.pse.tixclick.utils.AppUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class SeatServiceImpl implements SeatService {
    @Autowired
    ZoneRepository zoneRepository;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    ModelMapper mapper;

    @Autowired
    AppUtils appUtils;

    @Autowired
    SeatMapRepository seatMapRepository;


    @Override
    public SeatDTO createSeat(CreateSeatRequest createSeatRequest) {
        Zone zone = zoneRepository
                .findById(createSeatRequest.getZoneId())
                .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

        Seat seat = new Seat();
        seat.setColumnNumber(createSeatRequest.getColumnNumber());
        seat.setRowNumber(createSeatRequest.getRowNumber());
        seat.setCreatedDate(LocalDateTime.now());
        seat.setUpdatedDate(null);
        seat.setZone(zone);

        seatRepository.save(seat);
        return mapper.map(seat, SeatDTO.class);
    }

    @Override
    public SeatDTO updateSeat(UpdateSeatRequest updateSeatRequest, int seatId) {
        Seat seat = seatRepository
                .findById(seatId)
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));

        Zone zone = zoneRepository
                .findById(updateSeatRequest.getZoneId())
                .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

        seat.setColumnNumber(updateSeatRequest.getColumnNumber());
        seat.setRowNumber(updateSeatRequest.getRowNumber());
        seat.setUpdatedDate(LocalDateTime.now());
        seat.setZone(zone);

        seatRepository.save(seat);
        return mapper.map(seat, SeatDTO.class);
    }

    @Override
    public List<SeatDTO> getAllSeats() {
        List<Seat> seats = seatRepository.findAll();

        return seats.stream()
                .map(seat -> mapper.map(seat, SeatDTO.class))
                .toList();
    }

    @Override
    public List<SeatDTO> getSeatsByZone(int zoneId) {
        List<Seat> seats = seatRepository.getSeatsBySeatMapId(zoneId);
        if(seats.isEmpty()) {
            throw new AppException(ErrorCode.SEAT_NOT_FOUND);
        }

        return seats.stream()
                .map(seat -> mapper.map(seat, SeatDTO.class))
                .toList();
    }

    @Override
    public List<SeatDTO> getSeatsBySeatMap(int seatMapId) {
        List<Seat> seats = seatRepository.getSeatsBySeatMapId(seatMapId);
        if(seats.isEmpty()) {
            throw new AppException(ErrorCode.SEAT_NOT_FOUND);
        }

        return seats.stream()
                .map(seat -> mapper.map(seat, SeatDTO.class))
                .toList();
    }

    @Override
    public void deleteSeat(int seatId) {
        Seat seat = seatRepository
                .findById(seatId)
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));

        seat.setStatus(false);
        seatRepository.save(seat);
    }
}
