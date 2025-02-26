package com.pse.tixclick.service.impl;

import com.pse.tixclick.payload.dto.ZoneDTO;
import com.pse.tixclick.payload.request.create.CreateZoneRequest;
import com.pse.tixclick.service.ZoneService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ZoneServiceImpl implements ZoneService {
    @Override
    public ZoneDTO createZone(CreateZoneRequest createZoneRequest) {
        return null;
    }
}
