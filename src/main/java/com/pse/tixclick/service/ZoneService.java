package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.ZoneDTO;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import com.pse.tixclick.payload.request.create.CreateZoneRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneService{
    ZoneDTO createZone(CreateZoneRequest createZoneRequest);
}
