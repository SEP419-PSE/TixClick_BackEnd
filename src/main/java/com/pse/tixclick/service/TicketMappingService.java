package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.TicketMappingDTO;
import com.pse.tixclick.payload.response.TicketMappingResponse;

import java.util.List;

public interface TicketMappingService {
    List<TicketMappingResponse> getAllTicketMappingByEventActivityId(int eventActivityId);
}
