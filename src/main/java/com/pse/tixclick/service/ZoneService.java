package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.ZoneDTO;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import com.pse.tixclick.payload.request.SectionRequest;
import com.pse.tixclick.payload.request.create.CreateZoneRequest;
import com.pse.tixclick.payload.request.update.UpdateZoneRequest;
import com.pse.tixclick.payload.response.SectionResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZoneService{
    ZoneDTO createZone(CreateZoneRequest createZoneRequest);

    ZoneDTO updateZone(UpdateZoneRequest updateZoneRequest, int zoneId);

    List<ZoneDTO> getAllZones();

    List<ZoneDTO> getZonesBySeatMap(int seatMapId);

    List<ZoneDTO> getZonesByType(int type);


}
