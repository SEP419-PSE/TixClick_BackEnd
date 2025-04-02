package com.pse.tixclick.service.impl;

import com.pse.tixclick.payload.entity.entity_enum.ESeatActivityStatus;
import com.pse.tixclick.payload.entity.entity_enum.ZoneTypeEnum;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.seatmap.*;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.request.SeatRequest;
import com.pse.tixclick.payload.request.SectionRequest;
import com.pse.tixclick.payload.response.GetSeatResponse;
import com.pse.tixclick.payload.response.GetSectionResponse;
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
import org.springframework.scheduling.annotation.Async;
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

    @Autowired
    EventActivityRepository eventActivityRepository;

    @Autowired
    ZoneActivityRepository zoneActivityRepository;

    @Autowired
    SeatActivityRepository seatActivityRepository;

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
        if (seatMaps.isEmpty()) {
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
    public List<SectionResponse> designZone(List<SectionRequest> sectionRequests, int eventId) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        seatMapRepository.findSeatMapByEvent_EventId(eventId).ifPresent(seatMap -> {
            List<Zone> zones = zoneRepository.findBySeatMapId(seatMap.getSeatMapId());

            if (!zones.isEmpty()) {
                List<Integer> zoneIds = zones.stream().map(Zone::getZoneId).toList();
                seatRepository.deleteSeatsByZoneIds(zoneIds);
                zoneRepository.deleteAll(zones);
                zoneRepository.flush();
            }

            seatMapRepository.delete(seatMap);
            seatMapRepository.flush();
        });

        SeatMap newSeatMap = new SeatMap();
        newSeatMap.setEvent(event);
        seatMapRepository.saveAndFlush(newSeatMap);

        Map<String, Ticket> ticketCache = ticketRepository.findAll().stream()
                .collect(Collectors.toMap(Ticket::getTicketCode, Function.identity()));

        List<SectionResponse> sectionResponses = new ArrayList<>();

        for (SectionRequest sectionRequest : sectionRequests) {
            ZoneTypeEnum zoneTypeEnum = Arrays.stream(ZoneTypeEnum.values())
                    .filter(e -> e.name().equalsIgnoreCase(sectionRequest.getType()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.ZONE_TYPE_NOT_FOUND));

            var zoneType = zoneTypeRepository.findZoneTypeByTypeName(zoneTypeEnum)
                    .orElseThrow(() -> new AppException(ErrorCode.ZONE_TYPE_NOT_FOUND));

            Zone zone = new Zone();
            zone.setZoneCode(sectionRequest.getId());
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

            Ticket ticket = ticketCache.get(sectionRequest.getPriceId());
            double price = (ticket != null) ? ticket.getPrice() : 0;
            if (!ZoneTypeEnum.SEATED.equals(zoneTypeEnum)) {
                if (ticket == null) throw new AppException(ErrorCode.TICKET_NOT_FOUND);
                zone.setTicket(ticket);
            }

            zoneRepository.save(zone);

            List<SeatResponse> seatResponses = new ArrayList<>();
            List<Seat> seats = new ArrayList<>();
            for (SeatRequest seatDto : sectionRequest.getSeats()) {
                Ticket seatTicket = ticketCache.get(seatDto.getSeatTypeId());
                if (seatTicket == null) throw new AppException(ErrorCode.TICKET_NOT_FOUND);

                Seat seat = new Seat();
                seat.setZone(zone);
                seat.setRowNumber(seatDto.getRow());
                seat.setColumnNumber(seatDto.getColumn());
                seat.setTicket(seatTicket);
                seat.setSeatName(seatDto.getId());
                seat.setCreatedDate(LocalDateTime.now());
                seat.setStatus(true);
                seats.add(seat);

                SeatResponse seatResponse = new SeatResponse();
                seatResponse.setId(seatDto.getId());
                seatResponse.setRow(seatDto.getRow());
                seatResponse.setColumn(seatDto.getColumn());
                seatResponse.setSeatTypeId(seatTicket.getTicketCode());
                seatResponse.setStatus(seat.isStatus());
                seatResponses.add(seatResponse);
            }
            seatRepository.saveAll(seats);

            SectionResponse sectionResponse = SectionResponse.builder()
                    .id(String.valueOf(zone.getZoneId()))
                    .name(zone.getZoneName())
                    .rows(Integer.parseInt(zone.getRows()))
                    .columns(Integer.parseInt(zone.getColumns()))
                    .x(Integer.parseInt(zone.getXPosition()))
                    .y(Integer.parseInt(zone.getYPosition()))
                    .width(Integer.parseInt(zone.getWidth()))
                    .height(Integer.parseInt(zone.getHeight()))
                    .capacity(zone.getQuantity())
                    .type(zone.getZoneType().getTypeName().name())
                    .priceId(zone.getTicket() != null ? zone.getTicket().getTicketCode() : null)
                    .price(price)
                    .seats(seatResponses)
                    .isSave(zone.isStatus())
                    .build();

            sectionResponses.add(sectionResponse);
        }

        return sectionResponses;
    }


    @Override
    public List<SectionResponse> getSectionsByEventId(int eventId) {
        var seatMap = seatMapRepository.findSeatMapByEvent_EventId(eventId);
        if (seatMap.isEmpty()) {
            return Collections.emptyList();
        }

        // Lấy danh sách các Zone thuộc SeatMap
        List<Zone> zones = zoneRepository.findBySeatMapId(seatMap.get().getSeatMapId());

        // Nếu không có zone nào, trả về danh sách rỗng
        if (zones.isEmpty()) {
            return Collections.emptyList();
        }

        // Chuyển đổi Zone entity thành SectionRequest DTO, bao gồm danh sách ghế và giá vé
        return zones.stream().map(zone -> {
            String ticketCode = (zone.getTicket() != null) ? zone.getTicket().getTicketCode() : null;

            // 🔥 Lấy giá tiền từ ticketCode
            double price = 0; // Mặc định nếu không có vé thì giá là 0
            if (ticketCode != null) {
                price = ticketRepository.findTicketByTicketCode(ticketCode)
                        .map(Ticket::getPrice)
                        .orElse(0.0); // Nếu không tìm thấy ticket thì để giá 0
            }

            return SectionResponse.builder()
                    .id(String.valueOf(zone.getZoneId()))
                    .name(zone.getZoneName())
                    .rows(Integer.parseInt(zone.getRows()))
                    .columns(Integer.parseInt(zone.getColumns()))
                    .x(Integer.parseInt(zone.getXPosition()))
                    .y(Integer.parseInt(zone.getYPosition()))
                    .width(Integer.parseInt(zone.getWidth()))
                    .height(Integer.parseInt(zone.getHeight()))
                    .capacity(zone.getQuantity())
                    .type(zone.getZoneType().getTypeName().name()) // Lấy tên enum ZoneType
                    .priceId(ticketCode) // Ticket Code của vé
                    .price(price) // Giá tiền lấy từ ticket
                    .seats(getSeatsByZoneId(zone.getZoneId())) // Gọi hàm lấy danh sách ghế
                    .isSave(zone.isStatus())
                    .build();
        }).collect(Collectors.toList());
    }


    @Override
    public List<SeatResponse> getSeatsByZoneId(int zoneId) {
        List<Seat> seats = seatRepository.findSeatsByZone_ZoneId(zoneId);

        // Nếu không có ghế nào, trả về danh sách rỗng
        if (seats.isEmpty()) {
            return Collections.emptyList();
        }

        // Chuyển đổi từ Seat entity sang SeatRequest DTO
        return seats.stream().map(seat -> SeatResponse.builder()
                .id(seat.getSeatName())
                .row(seat.getRowNumber())
                .column(seat.getColumnNumber())
                .seatTypeId(seat.getTicket() != null ? seat.getTicket().getTicketCode() : null)
                .status(seat.isStatus())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<SectionResponse> deleteZone(List<SectionRequest> sectionResponse, String zoneId, int eventId) {
        designZone(sectionResponse, eventId);
        Zone zone = zoneRepository.findZoneByZoneCode(zoneId)
                .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));
        zoneRepository.delete(zone);

        return getSectionsByEventId(eventId);

    }


    @Override
    public List<GetSectionResponse> getSections(int eventId, int eventActivityId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        EventActivity eventActivity = eventActivityRepository.findById(eventActivityId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_ACTIVITY_NOT_FOUND));

        List<ZoneActivity> zoneActivityList = zoneActivityRepository.findZoneActivitiesByEventActivity_EventActivityId(eventActivityId);
        if (zoneActivityList.isEmpty()) {
            return Collections.emptyList();
        }

        List<GetSectionResponse> sectionResponses = new ArrayList<>();

        for (ZoneActivity zoneActivity : zoneActivityList) {
            Zone zone = zoneActivity.getZone();
            ZoneTypeEnum zoneTypeEnum = zone.getZoneType().getTypeName();
            List<SeatActivity> seatActivityList = new ArrayList<>();

            if (ZoneTypeEnum.SEATED.equals(zoneTypeEnum)) {
                seatActivityList = seatActivityRepository.findSeatActivitiesByZoneActivity_ZoneActivityId(zoneActivity.getZoneActivityId());
            }

            List<GetSeatResponse> seatResponses = new ArrayList<>();


            for (SeatActivity seatActivity : seatActivityList) {
                Seat seat = seatActivity.getSeat();
                GetSeatResponse seatResponse = new GetSeatResponse();
                seatResponse.setSeatId(seat.getSeatId());
                seatResponse.setId(seat.getSeatName());
                seatResponse.setRow(seat.getRowNumber());
                seatResponse.setColumn(seat.getColumnNumber());
                seatResponse.setSeatTypeId(seat.getTicket().getTicketCode());

                if (seatActivity.getStatus().equals(ESeatActivityStatus.SOLD.name())) {
                    seatResponse.setStatus(false);
                } else if (ESeatActivityStatus.valueOf(seatActivity.getStatus()) == ESeatActivityStatus.AVAILABLE) {
                    seatResponse.setStatus(true);

                } else if (seatActivity.getStatus().equals(ESeatActivityStatus.PENDING.name())) {
                    seatResponse.setStatus(false);
                }


                seatResponses.add(seatResponse);
            }

            int availableSeatsCount = zone.getQuantity();
            GetSectionResponse sectionResponse = GetSectionResponse.builder()
                    .zoneId(zone.getZoneId())
                    .id(String.valueOf(zone.getZoneId()))
                    .name(zone.getZoneName())
                    .rows(Integer.parseInt(zone.getRows()))
                    .columns(Integer.parseInt(zone.getColumns()))
                    .x(Integer.parseInt(zone.getXPosition()))
                    .y(Integer.parseInt(zone.getYPosition()))
                    .width(Integer.parseInt(zone.getWidth()))
                    .height(Integer.parseInt(zone.getHeight()))
                    .capacity(availableSeatsCount)
                    .type(zoneTypeEnum.name())
                    .priceId(zone.getTicket() != null ? zone.getTicket().getTicketCode() : null)
                    .price(zone.getTicket() != null ? zone.getTicket().getPrice() : 0)
                    .seats(seatResponses)
                    .isSave(zone.isStatus())
                    .build();

            sectionResponses.add(sectionResponse);
        }
        return sectionResponses;
    }

}
