package com.pse.tixclick.service;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.entity.*;
import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.company.Contract;
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

    @Mock
    private ContractRepository contractRepository;

    @BeforeEach
    void setUp() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);

        lenient().when(authentication.getName()).thenReturn("testUser");
        lenient().when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
    }

    @Test
    void shouldThrowExceptionWhenRequestListIsNull() {
        assertThrows(IllegalArgumentException.class, () -> eventActivityService.createEventActivityAndTicket(null, "CONTRACT1"));
    }

    @Test
    void shouldThrowExceptionWhenRequestListIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> eventActivityService.createEventActivityAndTicket(new ArrayList<>(), "CONTRACT1"));
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.empty());

        CreateEventActivityAndTicketRequest req = new CreateEventActivityAndTicketRequest();
        req.setEventId(1);

        assertThrows(AppException.class, () -> eventActivityService.createEventActivityAndTicket(List.of(req), "CONTRACT1"));
    }

    @Test
    void shouldThrowWhenEventNotFound() {
        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(new Account()));
        when(eventRepository.findById(1)).thenReturn(Optional.empty());

        CreateEventActivityAndTicketRequest req = new CreateEventActivityAndTicketRequest();
        req.setEventId(1);

        assertThrows(AppException.class, () -> eventActivityService.createEventActivityAndTicket(List.of(req), "CONTRACT1"));
    }

    @Test
    void shouldThrowWhenMemberNotOwnerOrAdminInDraftEvent() {
        Account account = new Account();
        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));

        Event event = new Event();
        event.setStatus(EEventStatus.DRAFT);
        Company company = new Company();
        company.setCompanyId(10);
        event.setCompany(company);

        Member member = new Member();
        member.setSubRole(ESubRole.EMPLOYEE);

        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId("testUser", 10))
                .thenReturn(Optional.of(member));

        CreateEventActivityAndTicketRequest req = new CreateEventActivityAndTicketRequest();
        req.setEventId(1);

        assertThrows(AppException.class, () -> eventActivityService.createEventActivityAndTicket(List.of(req), "CONTRACT1"));
    }

    @Test
    void shouldThrowWhenContractCodeNullInScheduledEvent() {
        Account account = new Account();
        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));

        Event event = new Event();
        event.setStatus(EEventStatus.SCHEDULED);

        when(eventRepository.findById(1)).thenReturn(Optional.of(event));

        CreateEventActivityAndTicketRequest req = new CreateEventActivityAndTicketRequest();
        req.setEventId(1);

        assertThrows(AppException.class, () -> eventActivityService.createEventActivityAndTicket(List.of(req), null));
    }

    @Test
    void shouldThrowWhenContractNotFoundInScheduledEvent() {
        Account account = new Account();
        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));

        Event event = new Event();
        event.setStatus(EEventStatus.SCHEDULED);

        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(contractRepository.findByContractCode("CONTRACT1")).thenReturn(null);

        CreateEventActivityAndTicketRequest req = new CreateEventActivityAndTicketRequest();
        req.setEventId(1);

        assertThrows(AppException.class, () -> eventActivityService.createEventActivityAndTicket(List.of(req), "CONTRACT1"));
    }

    @Test
    void shouldThrowWhenAccountIsNotManagerInScheduledEvent() {
        Account account = new Account();
        account.setAccountId(1);
        Role role = new Role();
        role.setRoleName(ERole.BUYER);
        account.setRole(role);

        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));

        Event event = new Event();
        event.setStatus(EEventStatus.SCHEDULED);

        when(eventRepository.findById(1)).thenReturn(Optional.of(event));

        Contract contract = new Contract();
        Account contractOwner = new Account();
        contractOwner.setAccountId(1);
        contract.setAccount(contractOwner);

        when(contractRepository.findByContractCode("CONTRACT1")).thenReturn(contract);

        CreateEventActivityAndTicketRequest req = new CreateEventActivityAndTicketRequest();
        req.setEventId(1);

        assertThrows(AppException.class, () -> eventActivityService.createEventActivityAndTicket(List.of(req), "CONTRACT1"));
    }

    @Test
    void shouldThrowWhenEventStatusInvalid() {
        Account account = new Account();
        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));

        Event event = new Event();
        event.setStatus(EEventStatus.APPROVED);

        when(eventRepository.findById(1)).thenReturn(Optional.of(event));

        CreateEventActivityAndTicketRequest req = new CreateEventActivityAndTicketRequest();
        req.setEventId(1);

        assertThrows(AppException.class, () -> eventActivityService.createEventActivityAndTicket(List.of(req), "CONTRACT1"));
    }

//    @Test
//    void shouldCreateNewEventActivityAndTicketsSuccessfully() {
//        // Setup Account
//        Account account = new Account();
//        account.setAccountId(1);
//        Role role = new Role();
//        role.setRoleName(ERole.MANAGER);
//        account.setRole(role);
//
//        // Setup Event
//        Event event = new Event();
//        event.setEventId(1);
//        event.setStatus(EEventStatus.SCHEDULED);
//        Company company = new Company();
//        company.setCompanyId(100);
//        event.setCompany(company);
//
//        // Setup Contract
//        Contract contract = new Contract();
//        Account contractAccount = new Account();
//        contractAccount.setAccountId(1);
//        contract.setAccount(contractAccount);
//
//        // Request Ticket
//        CreateEventActivityAndTicketRequest.TicketRequest ticketReq = new CreateEventActivityAndTicketRequest.TicketRequest();
//        ticketReq.setTicketCode("CODE123");
//        ticketReq.setTicketName("Vé VIP");
//        ticketReq.setPrice(200000);
//        ticketReq.setMinQuantity(1);
//        ticketReq.setMaxQuantity(5);
//        ticketReq.setQuantity(10);
//
//        CreateEventActivityAndTicketRequest req = new CreateEventActivityAndTicketRequest();
//        req.setEventId(1);
//        req.setActivityName("New Activity");
//        req.setTickets(List.of(ticketReq));
//
//        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
//        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
//        when(contractRepository.findByContractCode("CONTRACT1")).thenReturn(contract);
//        when(eventActivityRepository.findEventActivitiesByEvent_EventId(1)).thenReturn(Collections.emptyList());
//        when(ticketRepository.findTicketByTicketCode("CODE123")).thenReturn(Optional.empty());
//        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
//        when(ticketMappingRepository.save(any(TicketMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));
//        when(eventActivityRepository.saveAndFlush(any(EventActivity.class))).thenAnswer(invocation -> {
//            EventActivity savedActivity = invocation.getArgument(0);
//            savedActivity.setEventActivityId(100);
//            return savedActivity;
//        });
//
//        List<CreateEventActivityAndTicketRequest> result = eventActivityService.createEventActivityAndTicket(List.of(req), "CONTRACT1");
//
//        assertEquals(1, result.size());
//        assertEquals(100, result.get(0).getEventActivityId());
//    }
//
//    @Test
//    void testUpdateEventActivity_whenEventActivityIdExists_shouldUpdateSuccessfully() {
//        // Arrange
//        String contractCode = "CONTRACT_123";
//        String username = "test_user";
//
//        var event = Event.builder()
//                .eventId(1)
//                .status(EEventStatus.SCHEDULED)
//                .company(Company.builder()
//                        .companyId(100)
//                        .representativeId(Account.builder()
//                                .accountId(1)
//                                .role(Role.builder().roleName(ERole.MANAGER).build())
//                                .build())
//                        .build())
//                .build();
//
//        var account = Account.builder()
//                .accountId(1)
//                .role(Role.builder().roleName(ERole.MANAGER).build())
//                .build();
//
//        var request = CreateEventActivityAndTicketRequest.builder()
//                .eventId(1)
//                .eventActivityId(100) // có id => update
//                .activityName("Updated Activity Name")
//                .dateEvent(LocalDate.of(2025, 5, 1))
//                .startTimeEvent(LocalTime.of(9, 0))
//                .endTimeEvent(LocalTime.of(17, 0))
//                .startTicketSale(LocalDateTime.of(2025, 4, 25, 9, 0))
//                .endTicketSale(LocalDateTime.of(2025, 4, 30, 17, 0))
//                .build();
//
//        var existingEventActivity = EventActivity.builder()
//                .eventActivityId(100)
//                .build();
//
//        // Mock SecurityContextHolder
//        var authentication = mock(Authentication.class);
//        when(authentication.getName()).thenReturn(username);
//        var context = mock(SecurityContext.class);
//        when(context.getAuthentication()).thenReturn(authentication);
//        SecurityContextHolder.setContext(context);
//
//        when(accountRepository.findAccountByUserName(username)).thenReturn(Optional.of(account));
//        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
//        when(contractRepository.findByContractCode(contractCode)).thenReturn(Contract.builder().account(account).build());
//        when(eventActivityRepository.findById(100)).thenReturn(Optional.of(existingEventActivity));
//        when(eventActivityRepository.saveAndFlush(any(EventActivity.class))).thenReturn(existingEventActivity);
//
//        // Act
//        List<CreateEventActivityAndTicketRequest> savedRequests = eventActivityService.createEventActivityAndTicket(
//                List.of(request), contractCode);
//
//        // Assert
//        assertEquals(1, savedRequests.size());
//        assertEquals(100, savedRequests.get(0).getEventActivityId());
//
//        verify(eventActivityRepository).findById(100);
//        verify(eventActivityRepository).saveAndFlush(existingEventActivity);
//
//        // Optional: kiểm tra fields đã được set đúng nếu cần
//        assertEquals("Updated Activity Name", existingEventActivity.getActivityName());
//        assertEquals(LocalDate.of(2025, 5, 1), existingEventActivity.getDateEvent());
//    }
//

}
