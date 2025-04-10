package com.pse.tixclick.service.impl;

import com.pse.tixclick.payload.dto.TicketDTO;
import com.pse.tixclick.payload.dto.TicketMappingDTO;
import com.pse.tixclick.payload.entity.seatmap.SeatMap;
import com.pse.tixclick.payload.entity.seatmap.ZoneActivity;
import com.pse.tixclick.payload.entity.ticket.TicketMapping;
import com.pse.tixclick.payload.response.TicketMappingResponse;
import com.pse.tixclick.repository.EventActivityRepository;
import com.pse.tixclick.repository.SeatMapRepository;
import com.pse.tixclick.repository.TicketMappingRepository;
import com.pse.tixclick.repository.ZoneActivityRepository;
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
    @Autowired
    private ZoneActivityRepository zoneActivityRepository;
    @Autowired
    private SeatMapRepository seatMapRepository;
    @Autowired
    private EventActivityRepository eventActivityRepository;
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

    @Override
    public boolean checkTicketMappingExist(int eventActivityId, int ticketId) {
        List<TicketMapping> ticketMappings = ticketMappingRepository.findTicketMappingsByEventActivity_EventActivityIdAndTicket_TicketIdAndQuantity(eventActivityId, ticketId, 0);
        if (ticketMappings.isEmpty()) {
            return true;
        } else {
            var eventActivity = eventActivityRepository.findById(eventActivityId);

            boolean isHaveSeatMap = seatMapRepository.findSeatMapByEvent_EventId(eventActivity.get().getEvent().getEventId()).isPresent();
            if(isHaveSeatMap){
                List<ZoneActivity> zoneActivityList = zoneActivityRepository.findZoneActivitiesByEventActivity_EventActivityIdAndZone_Ticket_TicketId(eventActivityId, ticketId);
                for (ZoneActivity zoneActivity : zoneActivityList) {
                    if (zoneActivity.getAvailableQuantity() < 0) {
                        return false;
                    } else {
                        return true;
                    }
                }

            }else {
                return false;
            }


        }
        return false;
    }
}
