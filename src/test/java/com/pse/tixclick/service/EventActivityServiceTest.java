package com.pse.tixclick.service;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.company.Member;
import com.pse.tixclick.payload.entity.entity_enum.EEventStatus;
import com.pse.tixclick.payload.entity.entity_enum.ERole;
import com.pse.tixclick.payload.entity.entity_enum.ESubRole;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketMapping;
import com.pse.tixclick.payload.request.CreateEventActivityAndTicketRequest;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.impl.EventActivityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EventActivityServiceTest {

    @InjectMocks
    private EventActivityServiceImpl eventActivityService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventActivityRepository eventActivityRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketMappingRepository ticketMappingRepository;

    @Mock
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        // Mock SecurityContext và Authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);

        lenient().when(authentication.getName()).thenReturn("testUser");
        lenient().when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
    }

//    @Test
//    void shouldThrowExceptionWhenRequestListIsNull() {
//        assertThrows(IllegalArgumentException.class, () -> eventActivityService.createEventActivityAndTicket(null));
//    }
//    @Test
//    void shouldThrowExceptionWhenRequestListIsEmpty() {
//        assertThrows(IllegalArgumentException.class, () -> eventActivityService.createEventActivityAndTicket(new ArrayList<>()));
//    }
//
//    @Test
//    void shouldThrowWhenAccountNotFound() {
//        when(accountRepository.findAccountByUserName("testUser"))
//                .thenReturn(Optional.empty());
//
//        CreateEventActivityAndTicketRequest req = new CreateEventActivityAndTicketRequest();
//        req.setEventId(1);
//
//        List<CreateEventActivityAndTicketRequest> list = List.of(req);
//
//        assertThrows(AppException.class, () -> eventActivityService.createEventActivityAndTicket(list));
//    }
//
//    @Test
//    void shouldThrowWhenEventNotFound() {
//        Account account = new Account(); // khởi tạo tài khoản
//        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
//        when(eventRepository.findById(1)).thenReturn(Optional.empty());
//
//        CreateEventActivityAndTicketRequest req = new CreateEventActivityAndTicketRequest();
//        req.setEventId(1);
//        List<CreateEventActivityAndTicketRequest> list = List.of(req);
//
//        assertThrows(AppException.class, () -> eventActivityService.createEventActivityAndTicket(list));
//    }
//
//    @Test
//    void shouldThrowWhenNotOwnerOrAdminInDraftEvent() {
//        Account account = new Account(); // account của người dùng đang đăng nhập
//        Event event = new Event();
//        event.setStatus(EEventStatus.DRAFT);
//        Company company = new Company();
//        company.setCompanyId(10);
//        event.setCompany(company);
//
//        Member member = new Member();
//        member.setSubRole(ESubRole.EMPLOYEE); // không phải OWNER/ADMIN
//
//        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
//        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
//        when(memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId("testUser", 10))
//                .thenReturn(Optional.of(member));
//
//        CreateEventActivityAndTicketRequest req = new CreateEventActivityAndTicketRequest();
//        req.setEventId(1);
//
//        assertThrows(AppException.class, () -> eventActivityService.createEventActivityAndTicket(List.of(req)));
//    }
//
//    @Test
//    void shouldThrowWhenNotManagerInScheduledEvent() {
//        Account account = new Account();
//        Role role = new Role();
//        role.setRoleName(ERole.BUYER); // không phải MANAGER
//        account.setRole(role);
//
//        Event event = new Event();
//        event.setStatus(EEventStatus.SCHEDULED);
//
//        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
//        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
//
//        CreateEventActivityAndTicketRequest req = new CreateEventActivityAndTicketRequest();
//        req.setEventId(1);
//
//        assertThrows(AppException.class, () -> eventActivityService.createEventActivityAndTicket(List.of(req)));
//    }
//    @Test
//    void shouldThrowWhenEventStatusIsInvalid() {
//        Account account = new Account();
//        Event event = new Event();
//        event.setStatus(EEventStatus.APPROVED); // không phải DRAFT/SCHEDULED
//
//        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
//        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
//
//        CreateEventActivityAndTicketRequest req = new CreateEventActivityAndTicketRequest();
//        req.setEventId(1);
//
//        assertThrows(AppException.class, () -> eventActivityService.createEventActivityAndTicket(List.of(req)));
//    }
//
//    @Test
//    void shouldCreateEventActivityAndTicketsSuccessfully() {
//        // Setup account
//        Account account = new Account();
//        Role role = new Role();
//        role.setRoleName(ERole.MANAGER);
//        account.setRole(role);
//
//        // Setup event
//        Event event = new Event();
//        event.setEventId(1);
//        event.setStatus(EEventStatus.SCHEDULED);
//
//        // TicketRequest
//        CreateEventActivityAndTicketRequest.TicketRequest ticketReq = new CreateEventActivityAndTicketRequest.TicketRequest();
//        ticketReq.setTicketName("Vé A");
//        ticketReq.setTicketCode("CODE123");
//        ticketReq.setPrice(100000);
//        ticketReq.setQuantity(10);
//        ticketReq.setMinQuantity(1);
//        ticketReq.setMaxQuantity(5);
//
//        // Request
//        CreateEventActivityAndTicketRequest req = new CreateEventActivityAndTicketRequest();
//        req.setEventId(1);
//        req.setActivityName("Activity 1");
//        req.setTickets(List.of(ticketReq));
//
//        // Stub repo
//        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
//        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
//        when(eventActivityRepository.findEventActivitiesByEvent_EventId(1)).thenReturn(List.of());
//        when(ticketRepository.findTicketByTicketCode("CODE123")).thenReturn(Optional.empty());
//        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
//        when(ticketMappingRepository.save(any(TicketMapping.class))).thenAnswer(inv -> inv.getArgument(0));
//        when(eventActivityRepository.saveAndFlush(any(EventActivity.class)))
//                .thenAnswer(inv -> {
//                    EventActivity e = inv.getArgument(0);
//                    e.setEventActivityId(99); // giả định ID
//                    return e;
//                });
//
//        var result = eventActivityService.createEventActivityAndTicket(List.of(req));
//        assertEquals(1, result.size());
//        assertEquals(99L, result.get(0).getEventActivityId());
//    }
//    @Test
//    void shouldDeleteExistingEventActivitiesTicketsAndMappings() {
//        // Setup
//        Account account = new Account();
//        Role role = new Role();
//        role.setRoleName(ERole.MANAGER);
//        account.setRole(role);
//
//        Event event = new Event();
//        event.setEventId(1);
//        event.setStatus(EEventStatus.SCHEDULED);
//
//        // EventActivity hiện tại
//        EventActivity existingActivity = new EventActivity();
//        existingActivity.setEventActivityId(100);
//
//        // TicketMapping của activity
//        Ticket ticket = new Ticket();
//        ticket.setTicketCode("TICKET123");
//
//        TicketMapping mapping = new TicketMapping();
//        mapping.setTicket(ticket);
//        mapping.setEventActivity(existingActivity);
//
//        // Request mới
//        CreateEventActivityAndTicketRequest.TicketRequest ticketReq = new CreateEventActivityAndTicketRequest.TicketRequest();
//        ticketReq.setTicketCode("NEW123");
//        ticketReq.setTicketName("VIP");
//        ticketReq.setPrice(500);
//        ticketReq.setQuantity(10);
//        ticketReq.setMinQuantity(1);
//        ticketReq.setMaxQuantity(5);
//
//        CreateEventActivityAndTicketRequest request = new CreateEventActivityAndTicketRequest();
//        request.setEventId(1);
//        request.setActivityName("New Activity");
//        request.setTickets(List.of(ticketReq));
//
//        // Stub dữ liệu
//        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
//        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
//        when(eventActivityRepository.findEventActivitiesByEvent_EventId(1)).thenReturn(List.of(existingActivity));
//        when(ticketMappingRepository.findTicketMappingsByEventActivity(existingActivity)).thenReturn(List.of(mapping));
//        when(ticketRepository.findTicketsByEvent_EventId(1)).thenReturn(List.of(ticket));
//        when(ticketMappingRepository.findTicketMappingsByTicket(ticket)).thenReturn(Optional.empty());
//
//        // Lưu event activity mới
//        when(eventActivityRepository.saveAndFlush(any(EventActivity.class))).thenAnswer(inv -> {
//            EventActivity e = inv.getArgument(0);
//            e.setEventActivityId(200);
//            return e;
//        });
//
//        // Ticket mới
//        when(ticketRepository.findTicketByTicketCode("NEW123")).thenReturn(Optional.empty());
//        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
//        when(ticketMappingRepository.save(any(TicketMapping.class))).thenAnswer(inv -> inv.getArgument(0));
//
//        // Act
//        eventActivityService.createEventActivityAndTicket(List.of(request));
//
//        // Assert: kiểm tra các phương thức xóa được gọi
//        verify(ticketMappingRepository, times(1)).delete(mapping);
//        verify(ticketRepository, times(1)).delete(ticket);
//        verify(eventActivityRepository, times(1)).delete(existingActivity);
//    }

}
