package com.pse.tixclick.service.impl;

import com.pse.tixclick.payload.dto.TicketDTO;
import com.pse.tixclick.payload.dto.TicketMappingDTO;
import com.pse.tixclick.payload.entity.ticket.TicketMapping;
import com.pse.tixclick.payload.response.TicketMappingResponse;
import com.pse.tixclick.repository.TicketMappingRepository;
import com.pse.tixclick.service.TicketMappingService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class TicketMappingServiceImpl implements TicketMappingService {
    @Autowired
    private TicketMappingRepository ticketMappingRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Override
    public List<TicketMappingResponse> getAllTicketMappingByEventActivityId(int eventActivityId) {
        List<TicketMapping> ticketMappings = ticketMappingRepository.findTicketMappingsByEventActivity_EventActivityId(eventActivityId);
        return ticketMappings.stream().map(ticketMapping -> {
            TicketMappingResponse ticketMappingDTO = new TicketMappingResponse();
            ticketMappingDTO.setId(ticketMapping.getTicketMappingId());
            ticketMappingDTO.setTicket(modelMapper.map(ticketMapping.getTicket(), TicketDTO.class));
            ticketMappingDTO.setQuantity(ticketMapping.getQuantity());
            ticketMappingDTO.setStatus(ticketMapping.isStatus());
            return ticketMappingDTO;
        }).toList();


    }
}
