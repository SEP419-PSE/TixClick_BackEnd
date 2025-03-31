package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.ZoneDTO;
import com.pse.tixclick.payload.entity.seatmap.Seat;
import com.pse.tixclick.payload.entity.seatmap.SeatMap;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import com.pse.tixclick.payload.entity.seatmap.ZoneType;
import com.pse.tixclick.payload.request.SectionRequest;
import com.pse.tixclick.payload.request.create.CreateZoneRequest;
import com.pse.tixclick.payload.request.update.UpdateZoneRequest;
import com.pse.tixclick.payload.response.SectionResponse;
import com.pse.tixclick.repository.SeatMapRepository;
import com.pse.tixclick.repository.SeatRepository;
import com.pse.tixclick.repository.ZoneRepository;
import com.pse.tixclick.repository.ZoneTypeRepository;
import com.pse.tixclick.service.SeatMapService;
import com.pse.tixclick.service.ZoneService;
import com.pse.tixclick.utils.AppUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ZoneServiceImpl implements ZoneService {
    @Autowired
    ZoneRepository zoneRepository;

    @Autowired
    ZoneTypeRepository zoneTypeRepository;

    @Autowired
    ModelMapper mapper;

    @Autowired
    AppUtils appUtils;

    @Autowired
    SeatMapRepository seatMapRepository;

    @Autowired
    SeatRepository seatRepository;
    @Autowired
    SeatMapService seatMapService;





    @Override
    public ZoneDTO createZone(CreateZoneRequest createZoneRequest) {
        SeatMap seatMap = seatMapRepository
                .findById(createZoneRequest.getSeatMapId())
                .orElseThrow(() -> new AppException(ErrorCode.SEATMAP_NOT_FOUND));

        ZoneType zoneType = zoneTypeRepository
                .findById(createZoneRequest.getZoneTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.ZONE_TYPE_NOT_FOUND));

        Zone zone = new Zone();
        zone.setZoneName(createZoneRequest.getZoneName());
        zone.setZoneType(zoneType);
        zone.setSeatMap(seatMap);
        zone.setColumns(createZoneRequest.getColumns());
        zone.setRows(createZoneRequest.getRows());
//        zone.setAvailableQuantity(createZoneRequest.getAvailableQuantity());
        zone.setHeight(createZoneRequest.getHeight());
        zone.setWidth(createZoneRequest.getWidth());
        zone.setQuantity(createZoneRequest.getQuantity());
        zone.setStatus(true);
        zone.setCreatedDate(LocalDateTime.now());
        zone.setUpdatedDate(null);
        zone.setXPosition(createZoneRequest.getXPosition());
        zone.setYPosition(createZoneRequest.getYPosition());

        zoneRepository.save(zone);
        return mapper.map(zone, ZoneDTO.class);
    }

    @Override
    public ZoneDTO updateZone(UpdateZoneRequest updateZoneRequest, int zoneId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new AppException(ErrorCode.ZONE_NOT_FOUND));

        zone.setZoneName(updateZoneRequest.getZoneName());
//        zone.setAvailableQuantity(updateZoneRequest.getAvailableQuantity());
        zone.setQuantity(updateZoneRequest.getQuantity());
        zone.setRows(updateZoneRequest.getRows());
        zone.setColumns(updateZoneRequest.getColumns());
        zone.setHeight(updateZoneRequest.getHeight());
        zone.setWidth(updateZoneRequest.getWidth());
        zone.setXPosition(updateZoneRequest.getXPosition());
        zone.setYPosition(updateZoneRequest.getYPosition());
        zone.setUpdatedDate(LocalDateTime.now());

        zoneRepository.save(zone);
        return mapper.map(zone, ZoneDTO.class);
    }

    @Override
    public List<ZoneDTO> getAllZones() {
        List<Zone> zones = zoneRepository.findAll();

        return zones.stream()
                .map(zone -> mapper.map(zone, ZoneDTO.class))
                .toList();
    }



    @Override
    public List<ZoneDTO> getZonesBySeatMap(int seatMapId) {
        List<Zone> zones = zoneRepository.findBySeatMapId(seatMapId);
        if(zones.isEmpty()) {
            throw new AppException(ErrorCode.ZONE_NOT_FOUND);
        }
        return zones.stream()
                .map(zone -> mapper.map(zone, ZoneDTO.class))
                .toList();
    }

    @Override
    public List<ZoneDTO> getZonesByType(int type) {
        List<Zone> zones = zoneRepository.findByZoneType(type);
        if(zones.isEmpty()) {
            throw new AppException(ErrorCode.ZONE_NOT_FOUND);
        }
        return zones.stream()
                .map(zone -> mapper.map(zone, ZoneDTO.class))
                .toList();
    }




}
