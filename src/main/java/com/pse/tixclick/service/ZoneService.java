package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.ZoneDTO;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import com.pse.tixclick.payload.request.create.CreateZoneRequest;
import com.pse.tixclick.payload.request.update.UpdateZoneRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZoneService{
    ZoneDTO createZone(CreateZoneRequest createZoneRequest);

    ZoneDTO updateZone(UpdateZoneRequest updateZoneRequest, int zoneId);

    List<ZoneDTO> getAllZones();

    void deleteZone(int zoneId);

    List<ZoneDTO> getZonesBySeatMap(int seatMapId);

    List<ZoneDTO> getZonesByType(int type);
}
