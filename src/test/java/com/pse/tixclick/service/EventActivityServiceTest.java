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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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
//    void shouldThrowExceptionWhenRequestListIsEmpty() {
//        List<CreateEventActivityAndTicketRequest> requestList = new ArrayList<>();
//
//        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
//                eventActivityService.createEventActivityAndTicket(requestList));
//
//        assertEquals("Request list cannot be empty", ex.getMessage());
//    }
//
//    @Test
//    void shouldThrowExceptionWhenAccountNotFound() {
//        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.empty());
//
//        List<CreateEventActivityAndTicketRequest> requestList = List.of(
//                CreateEventActivityAndTicketRequest.builder().eventId(1).build()
//        );
//
//        AppException ex = assertThrows(AppException.class, () ->
//                eventActivityService.createEventActivityAndTicket(requestList));
//
//        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, ex.getErrorCode());
//    }
//
//    @Test
//    void shouldThrowExceptionWhenMemberNotFound() {
//        Account account = new Account();
//        Role role = new Role();
//        role.setRoleName(ERole.MANAGER);
//        account.setRole(role);
//
//        // GÁN COMPANY CHO ACCOUNT
//        Company company = new Company();
//        company.setCompanyId(1); // gán ID tùy ý
//        account.setCompany(company);
//
//        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
//        when(memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId("testUser", 1))
//                .thenReturn(Optional.empty());
//
//        List<CreateEventActivityAndTicketRequest> requestList = List.of(
//                CreateEventActivityAndTicketRequest.builder().eventId(1).build()
//        );
//
//        AppException ex = assertThrows(AppException.class, () ->
//                eventActivityService.createEventActivityAndTicket(requestList));
//
//        assertEquals(ErrorCode.MEMBER_NOT_FOUND, ex.getErrorCode());
//    }
//
//
//    @Test
//    void shouldThrowExceptionWhenUserNotPermission() {
//        Account account = new Account();
//        Role role = new Role();
//        role.setRoleName(ERole.MANAGER);
//        account.setRole(role);
//
//        Company company = new Company();
//        company.setCompanyId(1);
//        account.setCompany(company);
//
//        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
//
//        Member member = new Member();
//        member.setSubRole(ESubRole.EMPLOYEE); // Không phải OWNER hay ADMIN
//
//        when(memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId("testUser", 1))
//                .thenReturn(Optional.of(member));
//
//        List<CreateEventActivityAndTicketRequest> requestList = List.of(
//                CreateEventActivityAndTicketRequest.builder().eventId(1).build()
//        );
//
//        AppException ex = assertThrows(AppException.class, () ->
//                eventActivityService.createEventActivityAndTicket(requestList));
//
//        assertEquals(ErrorCode.NOT_PERMISSION, ex.getErrorCode());
//    }
//
//
//    @Test
//    void shouldThrowExceptionWhenEventNotFound() {
//        // Giả lập tài khoản với công ty
//        Account account = new Account();
//        Role role = new Role();
//        role.setRoleName(ERole.MANAGER); // Giả lập role là MANAGER
//        account.setRole(role);
//
//        // Tạo công ty và gán cho tài khoản
//        Company company = new Company();
//        company.setCompanyId(1); // Giả lập companyId với kiểu Long
//        account.setCompany(company); // Gán công ty cho tài khoản
//
//        // Giả lập thành viên trong repository
//        Member member = new Member();
//        member.setSubRole(ESubRole.ADMIN); // Giả lập subRole
//        when(memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId("testUser", 1))
//                .thenReturn(Optional.of(member)); // Chú ý kiểu companyId là Long
//
//        // Giả lập tài khoản trong repository
//        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
//
//        // Giả lập sự kiện không tồn tại trong repository
//        when(eventRepository.findById(1)).thenReturn(Optional.empty());
//
//        List<CreateEventActivityAndTicketRequest> requestList = List.of(
//                CreateEventActivityAndTicketRequest.builder().eventId(1).build()
//        );
//
//        // Kiểm tra khi không tìm thấy sự kiện
//        AppException ex = assertThrows(AppException.class, () ->
//                eventActivityService.createEventActivityAndTicket(requestList));
//
//        // Kiểm tra mã lỗi trả về
//        assertEquals(ErrorCode.EVENT_NOT_FOUND, ex.getErrorCode());
//    }
//
//
//    @Test
//    void shouldThrowUnauthorizedWhenNotManager() {
//        // Tạo tài khoản với role không phải là Manager
//        Account account = new Account();
//        Role role = new Role();
//        role.setRoleName(ERole.BUYER); // Không phải Manager
//        account.setRole(role);
//
//        // Gán công ty cho tài khoản
//        Company company = new Company();
//        company.setCompanyId(1); // Gán companyId cho công ty
//        account.setCompany(company); // Gán công ty vào tài khoản
//
//        // Giả lập repository trả về tài khoản này
//        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
//
//        // Giả lập repository trả về thành viên không tồn tại
//        when(memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId("testUser", 1))
//                .thenReturn(Optional.empty()); // Member không tồn tại
//
//        // Tạo sự kiện có trạng thái SCHEDULED
//        Event event = new Event();
//        event.setStatus(EEventStatus.SCHEDULED);
//        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
//
//        // Tạo request list hợp lệ
//        List<CreateEventActivityAndTicketRequest> requestList = List.of(
//                CreateEventActivityAndTicketRequest.builder().eventId(1).build()
//        );
//
//        // Kiểm tra xem hàm có ném exception MEMBER_NOT_FOUND không
//        AppException ex = assertThrows(AppException.class, () ->
//                eventActivityService.createEventActivityAndTicket(requestList));
//
//        // Kiểm tra mã lỗi trả về là MEMBER_NOT_FOUND
//        assertEquals(ErrorCode.MEMBER_NOT_FOUND, ex.getErrorCode());
//    }
//
//
//    @Test
//    void shouldCreateEventActivityAndTicketWhenRequestListIsValid() {
//        // Tạo role MANAGER
//        Role role = new Role();
//        role.setRoleName(ERole.MANAGER);
//
//        // Tạo công ty
//        Company company = new Company();
//        company.setCompanyId(1);
//
//        // Tạo account
//        Account account = new Account();
//        account.setUserName("testUser");
//        account.setRole(role);
//        account.setCompany(company);
//
//        // Tạo member có quyền
//        Member member = new Member();
//        member.setAccount(account);
//        member.setCompany(company);
//        member.setSubRole(ESubRole.OWNER); // ✅ Phải có quyền OWNER hoặc ADMIN
//
//        // Tạo event
//        Event event = new Event();
//        event.setStatus(EEventStatus.SCHEDULED);
//
//        // Request
//        List<CreateEventActivityAndTicketRequest> requestList = List.of(
//                CreateEventActivityAndTicketRequest.builder().eventId(1).build()
//        );
//
//        // Stub dữ liệu
//        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
//        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
//        when(memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId("testUser", 1))
//                .thenReturn(Optional.of(member));
//        when(eventActivityRepository.save(any())).thenReturn(new EventActivity());
//        when(ticketRepository.save(any())).thenReturn(new Ticket());
//
//        // Kiểm tra không ném exception
//        assertDoesNotThrow(() -> eventActivityService.createEventActivityAndTicket(requestList));
//    }
//
//    @Test
//    void shouldCreateTicketsAndMappingsFromRequest() {
//        // Setup mock data
//        CreateEventActivityAndTicketRequest request = CreateEventActivityAndTicketRequest.builder()
//                .eventId(1)
//                .activityName("Event Activity 1")
//                .dateEvent(LocalDate.of(2025, 5, 1))
//                .startTimeEvent(LocalTime.of(9, 0))
//                .endTimeEvent(LocalTime.of(12, 0))
//                .startTicketSale(LocalDateTime.of(2025, 5, 1, 8, 0))
//                .endTicketSale(LocalDateTime.of(2025, 5, 1, 18, 0))
//                .tickets(List.of(
//                        CreateEventActivityAndTicketRequest.TicketRequest.builder()
//                                .ticketName("VIP")
//                                .ticketCode("VIP001")
//                                .quantity(10)
//                                .price(100.0)
//                                .minQuantity(1)
//                                .maxQuantity(10)
//                                .build(),
//                        CreateEventActivityAndTicketRequest.TicketRequest.builder()
//                                .ticketName("NORMAL")
//                                .ticketCode("NORMAL001")
//                                .quantity(20)
//                                .price(50.0)
//                                .minQuantity(1)
//                                .maxQuantity(20)
//                                .build()
//                ))
//                .build();
//
//        List<CreateEventActivityAndTicketRequest> requestList = List.of(request);
//
//        // Mocking the necessary repositories
//        Event event = new Event();
//        event.setEventId(1);
//        event.setStatus(EEventStatus.SCHEDULED);
//
//        // Create a Company for the Account to avoid NullPointerException
//        Company company = new Company();
//        company.setCompanyId(1);  // Set the company ID
//
//        Account account = new Account();
//        account.setRole(new Role());
//        account.getRole().setRoleName(ERole.MANAGER);
//        account.setCompany(company);  // Set company for the account
//
//        Member member = new Member();
//        member.setSubRole(ESubRole.ADMIN);
//
//        // Mock the repository responses
//        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
//        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
//        when(memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId("testUser", 1)).thenReturn(Optional.of(member));
//
//        // Mock for saving event activity and ticket
//        when(ticketRepository.save(any(Ticket.class))).thenReturn(new Ticket());
//        when(ticketMappingRepository.save(any(TicketMapping.class))).thenReturn(new TicketMapping());
//
//        // Execute the method under test
//        List<CreateEventActivityAndTicketRequest> result = eventActivityService.createEventActivityAndTicket(requestList);
//
//        // Verify that save() was called for Ticket and TicketMapping
//        verify(ticketRepository, times(2)).save(any(Ticket.class)); // 2 tickets expected
//        verify(ticketMappingRepository, times(2)).save(any(TicketMapping.class)); // 2 mappings expected
//
//        // Verify that result is not empty
//        assertNotNull(result);
//        assertEquals(1, result.size(), "Expected 1 event activity to be created");
//
//        // Verify that the event activity id is correctly set
//        CreateEventActivityAndTicketRequest createdRequest = result.get(0);
//        assertEquals(0, createdRequest.getEventActivityId(), "Expected event activity ID to be 0 initially");
//    }
//
//    @Test
//    void shouldThrowUnauthorizedWhenAccountIsNotManager() {
//        // Tạo tài khoản với role không phải là Manager
//        Account account = new Account();
//        Role role = new Role();
//        role.setRoleName(ERole.BUYER); // Role là BUYER thay vì MANAGER
//        account.setRole(role);
//
//        // Giả lập repository trả về tài khoản này
//        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
//
//        // Tạo sự kiện có trạng thái SCHEDULED
//        Event event = new Event();
//        event.setStatus(EEventStatus.SCHEDULED);
//        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
//
//        // Tạo request list hợp lệ
//        List<CreateEventActivityAndTicketRequest> requestList = List.of(
//                CreateEventActivityAndTicketRequest.builder().eventId(1).build()
//        );
//
//        // Kiểm tra xem hàm có ném ngoại lệ UNAUTHORIZED không
//        AppException ex = assertThrows(AppException.class, () ->
//                eventActivityService.createEventActivityAndTicket(requestList)
//        );
//
//        // Kiểm tra mã lỗi trả về là UNAUTHORIZED
//        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
//    }
//

}
