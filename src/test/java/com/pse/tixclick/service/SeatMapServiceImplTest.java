package com.pse.tixclick.service;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.entity.entity_enum.ZoneTypeEnum;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.seatmap.SeatMap;
import com.pse.tixclick.payload.entity.seatmap.Zone;
import com.pse.tixclick.payload.entity.seatmap.ZoneType;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.request.SeatRequest;
import com.pse.tixclick.payload.request.SectionRequest;
import com.pse.tixclick.payload.response.SectionResponse;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.impl.SeatMapServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class SeatMapServiceImplTest {

    @InjectMocks
    private SeatMapServiceImpl seatMapService;

    @Mock
    private EventRepository eventRepository;
    @Mock
    private SeatMapRepository seatMapRepository;
    @Mock
    private ZoneRepository zoneRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private ZoneTypeRepository zoneTypeRepository;

    @BeforeEach
    void setUp() {

        Authentication authentication = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);

        lenient().when(authentication.getName()).thenReturn("testUser");
        lenient().when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
    }
    @Test
    void testDesignZone_success() {
        // Arrange
        int eventId = 1;
        Event event = new Event();
        event.setEventId(eventId);
        event.setTypeEvent("OFFLINE"); // quan trọng: event phải OFFLINE mới cho phép design

        SeatMap seatMap = new SeatMap();
        seatMap.setSeatMapId(1);
        seatMap.setEvent(event);

        ZoneType zoneType = new ZoneType();
        zoneType.setTypeName(ZoneTypeEnum.SEATED);

        Ticket ticket = new Ticket();
        ticket.setTicketCode("TICKET1");
        ticket.setPrice(100.0);

        SeatRequest seatRequest = SeatRequest.builder()
                .id("A1")
                .row(String.valueOf(1))
                .column(String.valueOf(1))
                .seatTypeId("TICKET1")
                .build();

        SectionRequest sectionRequest = SectionRequest.builder()
                .id("zone-1")
                .name("Zone A")
                .rows(1)
                .columns(1)
                .x(0)
                .y(0)
                .width(100)
                .height(100)
                .capacity(1)
                .type("SEATED")
                .priceId("TICKET1")
                .seats(List.of(seatRequest))
                .build();

        // Mock dữ liệu trả về đúng sự kiện và seatMap
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(seatMapRepository.findSeatMapByEvent_EventId(eventId)).thenReturn(Optional.of(seatMap));
        when(ticketRepository.findAll()).thenReturn(List.of(ticket));
        when(zoneTypeRepository.findZoneTypeByTypeName(ZoneTypeEnum.SEATED)).thenReturn(Optional.of(zoneType));

        // Act
        List<SectionResponse> responses = seatMapService.designZone(List.of(sectionRequest), eventId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        SectionResponse response = responses.get(0);
        assertEquals("Zone A", response.getName());
        assertEquals(1, response.getRows());
        assertEquals(1, response.getColumns());
        assertEquals(1, response.getSeats().size());

        verify(seatMapRepository, times(1)).saveAndFlush(any(SeatMap.class));
        verify(zoneRepository, times(1)).save(any(Zone.class));
        verify(seatRepository, times(1)).saveAll(anyList());
    }


    @Test
    void testDesignZone_eventNotFound() {
        int eventId = 1;
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            seatMapService.designZone(List.of(), eventId);
        });

        assertEquals(ErrorCode.EVENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testDesignZone_eventOnlineShouldThrowError() {
        int eventId = 1;
        Event event = new Event();
        event.setEventId(eventId);
        event.setTypeEvent("ONLINE");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        AppException exception = assertThrows(AppException.class, () -> {
            seatMapService.designZone(List.of(), eventId);
        });

        assertEquals(ErrorCode.EVENT_ONLINE, exception.getErrorCode());
    }





    @Test
    void testDesignZone_zoneTypeNotFound() {
        int eventId = 1;
        Event event = new Event();
        event.setEventId(eventId);
        event.setTypeEvent("OFFLINE");

        SectionRequest sectionRequest = SectionRequest.builder()
                .id("zone-1")
                .name("Zone A")
                .rows(1)
                .columns(1)
                .x(0)
                .y(0)
                .width(100)
                .height(100)
                .capacity(1)
                .type("UNKNOWN") // type không tồn tại
                .priceId("TICKET1")
                .seats(new ArrayList<>())
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(ticketRepository.findAll()).thenReturn(new ArrayList<>());

        AppException exception = assertThrows(AppException.class, () -> {
            seatMapService.designZone(List.of(sectionRequest), eventId);
        });

        assertEquals(ErrorCode.ZONE_TYPE_NOT_FOUND, exception.getErrorCode());
    }
}
