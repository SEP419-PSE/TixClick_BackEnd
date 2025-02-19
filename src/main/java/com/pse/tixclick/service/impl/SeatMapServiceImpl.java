package com.pse.tixclick.service.impl;

import com.pse.tixclick.config.Util;
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
import com.pse.tixclick.repository.BackgroundRepository;
import com.pse.tixclick.repository.EventRepository;
import com.pse.tixclick.repository.SeatMapRepository;
import com.pse.tixclick.service.SeatMapService;
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
public class SeatMapServiceImpl implements SeatMapService {
    @Autowired
    Util util;

    @Autowired
    ModelMapper mapper;

    @Autowired
    SeatMapRepository seatMapRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    BackgroundRepository backgroundRepository;

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
}
