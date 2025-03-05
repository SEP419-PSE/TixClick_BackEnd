package com.pse.tixclick.service.impl;

import com.pse.tixclick.payload.entity.seatmap.Seat;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.response.SeatResponse;
import com.pse.tixclick.payload.response.SectionResponse;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.utils.AppUtils;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.BackgroundDTO;
import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.dto.SeatMapDTO;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.seatmap.Background;
import com.pse.tixclick.payload.entity.seatmap.SeatMap;
import com.pse.tixclick.payload.request.create.CreateSeatMapRequest;
import com.pse.tixclick.payload.request.update.UpdateSeatMapRequest;
import com.pse.tixclick.service.SeatMapService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class SeatMapServiceImpl implements SeatMapService {
    @Autowired
    AppUtils appUtils;

    @Autowired
    ModelMapper mapper;

    @Autowired
    SeatMapRepository seatMapRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    BackgroundRepository backgroundRepository;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    ZoneRepository zoneRepository;

    @Autowired
    ZoneTypeRepository zoneTypeRepository;

    @Autowired
    TicketRepository ticketRepository;

    @Override
    public SeatMapDTO createSeatMap(CreateSeatMapRequest createSeatMapRequest) {
        SeatMap seatMap = new SeatMap();
        seatMap.setSeatMapName(createSeatMapRequest.getSeatMapName());
        seatMap.setSeatMapHeight(createSeatMapRequest.getSeatMapHeight());
        seatMap.setSeatMapWidth(createSeatMapRequest.getSeatMapWidth());
        seatMap.setCreatedAt(LocalDateTime.now());
        seatMap.setStatus(false);

        Event event = eventRepository
                .findById(createSeatMapRequest.getEventId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        seatMap.setEvent(event);

        Background background = backgroundRepository
                .findById(createSeatMapRequest.getBackgroundId())
                .orElseThrow(() -> new AppException(ErrorCode.BACKGROUND_NOT_FOUND));
        seatMap.setBackground(background);
        SeatMap savedSeatMap = seatMapRepository.save(seatMap);

        SeatMapDTO seatMapDTO = mapper.map(savedSeatMap, SeatMapDTO.class);
        seatMapDTO.setBackground(mapper.map(savedSeatMap.getBackground(), BackgroundDTO.class));
        seatMapDTO.setEvent(mapper.map(savedSeatMap.getEvent(), EventDTO.class));
        return seatMapDTO;
    }

    @Override
    public List<SeatMapDTO> getAllSeatMaps() {
        List<SeatMap> seatMaps = seatMapRepository.findAll();
        if(seatMaps.isEmpty()) {
            throw new AppException(ErrorCode.SEATMAP_NOT_FOUND);
        }
        return seatMaps.stream()
                .map(seatMap -> {
                    SeatMapDTO seatMapDTO = mapper.map(seatMap, SeatMapDTO.class);
                    seatMapDTO.setBackground(mapper.map(seatMap.getBackground(), BackgroundDTO.class));
                    seatMapDTO.setEvent(mapper.map(seatMap.getEvent(), EventDTO.class));
                    return seatMapDTO;
                })
                .toList();
    }

    @Override
    public SeatMapDTO getSeatMapByEventId(int eventId) {
        SeatMap seatMap = seatMapRepository.findSeatMapByEvent(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.SEATMAP_NOT_FOUND));
        SeatMapDTO seatMapDTO = mapper.map(seatMap, SeatMapDTO.class);
        seatMapDTO.setBackground(mapper.map(seatMap.getBackground(), BackgroundDTO.class));
        seatMapDTO.setEvent(mapper.map(seatMap.getEvent(), EventDTO.class));
        return seatMapDTO;
    }

    @Override
    public void deleteSeatMap(int seatMapId) {
        SeatMap seatMap = seatMapRepository.findById(seatMapId)
                .orElseThrow(() -> new AppException(ErrorCode.SEATMAP_NOT_FOUND));
        seatMapRepository.delete(seatMap);
    }

    @Override
    public SeatMapDTO updateSeatMap(UpdateSeatMapRequest updateSeatMapRequest, int seatMapId) {
        SeatMap seatMap = seatMapRepository.findById(seatMapId)
                .orElseThrow(() -> new AppException(ErrorCode.SEATMAP_NOT_FOUND));
        seatMap.setSeatMapName(updateSeatMapRequest.getSeatMapName());
        seatMap.setSeatMapHeight(updateSeatMapRequest.getSeatMapHeight());
        seatMap.setSeatMapWidth(updateSeatMapRequest.getSeatMapWidth());
        seatMap.setUpdatedAt(LocalDateTime.now());
        seatMap.setStatus(seatMap.isStatus());

        Event event = eventRepository
                .findById(updateSeatMapRequest.getEventId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        seatMap.setEvent(event);

        Background background = backgroundRepository
                .findById(updateSeatMapRequest.getBackgroundId())
                .orElseThrow(() -> new AppException(ErrorCode.BACKGROUND_NOT_FOUND));
        seatMap.setBackground(background);

        SeatMap updatedSeatMap = seatMapRepository.save(seatMap);
        SeatMapDTO seatMapDTO = mapper.map(updatedSeatMap, SeatMapDTO.class);
        seatMapDTO.setBackground(mapper.map(updatedSeatMap.getBackground(), BackgroundDTO.class));
        seatMapDTO.setEvent(mapper.map(updatedSeatMap.getEvent(), EventDTO.class));
        return seatMapDTO;
    }

    @Override
    public List<SectionResponse> designZone(List<SectionResponse> sectionResponse, int eventId) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Optional<SeatMap> seatmapOpt = seatMapRepository.findSeatMapByEvent_EventId(eventId);

        if (seatmapOpt.isPresent()) {
            SeatMap seatMap = seatmapOpt.get(); // Lấy SeatMap từ Optional

            // Lấy danh sách zone thuộc seatMap
            List<Zone> zones = zoneRepository.findBySeatMapId(seatMap.getSeatMapId());

            if (!zones.isEmpty()) { // Nếu có zones thì tiếp tục xóa
                // Xóa tất cả seats trước
                for (Zone zone : zones) {
                    List<Seat> seats = seatRepository.findSeatsByZone_ZoneId(zone.getZoneId());
                    if (!seats.isEmpty()) {
                        for (Seat seat : seats) {
                            seatRepository.delete(seat);
                            log.info("Deleted seat: {}", seat.getSeatId());
                        }
                    }
                    for (Zone zoneDelete : zones) {
                        zoneRepository.delete(zoneDelete);
                        log.info("Deleted zone: {}", zoneDelete.getZoneId());
                    }
                }

                // Cuối cùng, xóa seatMap
                seatMapRepository.delete(seatMap);
                seatMapRepository.flush(); // Đảm bảo xóa ngay
                log.info("Deleted seatMap: {}", seatMap.getSeatMapName());
            }

            // Tạo mới SeatMap
            SeatMap newSeatMap = new SeatMap();
            newSeatMap.setEvent(event);
            seatMapRepository.save(newSeatMap);

            // Duyệt qua danh sách SectionResponse để tạo Zone và Seat
            for (SectionResponse section : sectionResponse) {
                var zoneType = zoneTypeRepository.findById(section.getZoneTypeId())
                        .orElseThrow(() -> new AppException(ErrorCode.ZONE_TYPE_NOT_FOUND));

                Zone zone = new Zone();
                zone.setSeatMap(newSeatMap);
                zone.setZoneName(section.getName());
                zone.setZoneType(zoneType);
                zoneRepository.save(zone);

                // Tạo danh sách ghế cho Zone
                for (SeatResponse seatDto : section.getSeats()) {
                    var price = ticketRepository.findById(seatDto.getTicketId())
                            .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

                    Seat seat = new Seat();
                    seat.setZone(zone);
                    seat.setRowNumber(seatDto.getRowNumber());
                    seat.setColumnNumber(seatDto.getColumnNumber());
                    seat.setTicket(price);
                    seatRepository.save(seat);
                }
            }


        }
        return sectionResponse;
    }

}
