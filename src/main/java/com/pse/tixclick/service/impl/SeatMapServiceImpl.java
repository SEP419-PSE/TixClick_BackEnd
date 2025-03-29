package com.pse.tixclick.service.impl;

import com.pse.tixclick.payload.entity.entity_enum.ZoneTypeEnum;
import com.pse.tixclick.payload.entity.seatmap.Seat;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.request.SeatRequest;
import com.pse.tixclick.payload.request.SectionRequest;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public List<SectionRequest> designZone(List<SectionRequest> sectionRequests, int eventId) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        seatMapRepository.findSeatMapByEvent_EventId(eventId).ifPresent(seatMap -> {
            List<Zone> zones = zoneRepository.findBySeatMapId(seatMap.getSeatMapId());

            if (!zones.isEmpty()) {
                List<Integer> zoneIds = zones.stream().map(Zone::getZoneId).collect(Collectors.toList());
                seatRepository.deleteSeatsByZoneIds(zoneIds);
                zoneRepository.deleteAll(zones);
                zoneRepository.flush(); // Đẩy thay đổi xuống DB ngay
            }

            seatMapRepository.delete(seatMap);
            seatMapRepository.flush(); // Đẩy thay đổi xuống DB ngay
        });

        SeatMap newSeatMap = new SeatMap();
        newSeatMap.setEvent(event);
        seatMapRepository.saveAndFlush(newSeatMap); // Lưu ngay lập tức


        Map<String, Ticket> ticketCache = ticketRepository.findAll().stream()
                .collect(Collectors.toMap(Ticket::getTicketCode, Function.identity()));

        for (SectionRequest sectionRequest : sectionRequests) {
            ZoneTypeEnum zoneTypeEnum = Arrays.stream(ZoneTypeEnum.values())
                    .filter(e -> e.name().equalsIgnoreCase(sectionRequest.getType()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.ZONE_TYPE_NOT_FOUND));

            var zoneType = zoneTypeRepository.findZoneTypeByTypeName(zoneTypeEnum)
                    .orElseThrow(() -> new AppException(ErrorCode.ZONE_TYPE_NOT_FOUND));

            Zone zone = new Zone();
            zone.setSeatMap(newSeatMap);
            zone.setZoneName(sectionRequest.getName());
            zone.setZoneType(zoneType);
            zone.setXPosition(String.valueOf(sectionRequest.getX()));
            zone.setRows(String.valueOf(sectionRequest.getRows()));
            zone.setColumns(String.valueOf(sectionRequest.getColumns()));
            zone.setYPosition(String.valueOf(sectionRequest.getY()));
            zone.setWidth(String.valueOf(sectionRequest.getWidth()));
            zone.setHeight(String.valueOf(sectionRequest.getHeight()));
            zone.setStatus(true);
            zone.setCreatedDate(LocalDateTime.now());

            int quantity = ZoneTypeEnum.SEATED.equals(zoneTypeEnum)
                    ? sectionRequest.getRows() * sectionRequest.getColumns()
                    : sectionRequest.getCapacity();
            zone.setQuantity(quantity);

            if (!ZoneTypeEnum.SEATED.equals(zoneTypeEnum)) {
                Ticket ticket = ticketCache.get(sectionRequest.getPriceId());
                if (ticket == null) throw new AppException(ErrorCode.TICKET_NOT_FOUND);
                zone.setTicket(ticket);
            }

            zoneRepository.save(zone);

            List<Seat> seats = new ArrayList<>();
            for (SeatRequest seatDto : sectionRequest.getSeats()) {
                Ticket price = ticketCache.get(seatDto.getSeatTypeId());
                if (price == null) throw new AppException(ErrorCode.TICKET_NOT_FOUND);

                Seat seat = new Seat();
                seat.setZone(zone);
                seat.setRowNumber(seatDto.getRow());
                seat.setColumnNumber(seatDto.getColumn());
                seat.setTicket(price);
                seat.setSeatName(seatDto.getId());
                seat.setCreatedDate(LocalDateTime.now());

                seats.add(seat);
            }
            seatRepository.saveAll(seats);
        }

        return sectionRequests;
    }




}
