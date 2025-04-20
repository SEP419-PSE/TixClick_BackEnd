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

    @Test
    void shouldThrowExceptionWhenRequestListIsEmpty() {
        List<CreateEventActivityAndTicketRequest> requestList = new ArrayList<>();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                eventActivityService.createEventActivityAndTicket(requestList));

        assertEquals("Request list cannot be empty", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFound() {
        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.empty());

        List<CreateEventActivityAndTicketRequest> requestList = List.of(
                CreateEventActivityAndTicketRequest.builder().eventId(1).build()
        );

        AppException ex = assertThrows(AppException.class, () ->
                eventActivityService.createEventActivityAndTicket(requestList));

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void shouldThrowExceptionWhenMemberNotFound() {
        Account account = new Account();
        Role role = new Role();
        role.setRoleName(ERole.MANAGER);
        account.setRole(role);

        // GÁN COMPANY CHO ACCOUNT
        Company company = new Company();
        company.setCompanyId(1); // gán ID tùy ý
        account.setCompany(company);

        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
        when(memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId("testUser", 1))
                .thenReturn(Optional.empty());

        List<CreateEventActivityAndTicketRequest> requestList = List.of(
                CreateEventActivityAndTicketRequest.builder().eventId(1).build()
        );

        AppException ex = assertThrows(AppException.class, () ->
                eventActivityService.createEventActivityAndTicket(requestList));

        assertEquals(ErrorCode.MEMBER_NOT_FOUND, ex.getErrorCode());
    }


    @Test
    void shouldThrowExceptionWhenUserNotPermission() {
        Account account = new Account();
        Role role = new Role();
        role.setRoleName(ERole.MANAGER);
        account.setRole(role);

        Company company = new Company();
        company.setCompanyId(1);
        account.setCompany(company);

        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));

        Member member = new Member();
        member.setSubRole(ESubRole.EMPLOYEE); // Không phải OWNER hay ADMIN

        when(memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId("testUser", 1))
                .thenReturn(Optional.of(member));

        List<CreateEventActivityAndTicketRequest> requestList = List.of(
                CreateEventActivityAndTicketRequest.builder().eventId(1).build()
        );

        AppException ex = assertThrows(AppException.class, () ->
                eventActivityService.createEventActivityAndTicket(requestList));

        assertEquals(ErrorCode.NOT_PERMISSION, ex.getErrorCode());
    }


    @Test
    void shouldThrowExceptionWhenEventNotFound() {
        // Giả lập tài khoản với công ty
        Account account = new Account();
        Role role = new Role();
        role.setRoleName(ERole.MANAGER); // Giả lập role là MANAGER
        account.setRole(role);

        // Tạo công ty và gán cho tài khoản
        Company company = new Company();
        company.setCompanyId(1); // Giả lập companyId với kiểu Long
        account.setCompany(company); // Gán công ty cho tài khoản

        // Giả lập thành viên trong repository
        Member member = new Member();
        member.setSubRole(ESubRole.ADMIN); // Giả lập subRole
        when(memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId("testUser", 1))
                .thenReturn(Optional.of(member)); // Chú ý kiểu companyId là Long

        // Giả lập tài khoản trong repository
        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));

        // Giả lập sự kiện không tồn tại trong repository
        when(eventRepository.findById(1)).thenReturn(Optional.empty());

        List<CreateEventActivityAndTicketRequest> requestList = List.of(
                CreateEventActivityAndTicketRequest.builder().eventId(1).build()
        );

        // Kiểm tra khi không tìm thấy sự kiện
        AppException ex = assertThrows(AppException.class, () ->
                eventActivityService.createEventActivityAndTicket(requestList));

        // Kiểm tra mã lỗi trả về
        assertEquals(ErrorCode.EVENT_NOT_FOUND, ex.getErrorCode());
    }


    @Test
    void shouldThrowUnauthorizedWhenNotManager() {
        // Tạo tài khoản với role không phải là Manager
        Account account = new Account();
        Role role = new Role();
        role.setRoleName(ERole.BUYER); // Không phải Manager
        account.setRole(role);

        // Gán công ty cho tài khoản
        Company company = new Company();
        company.setCompanyId(1); // Gán companyId cho công ty
        account.setCompany(company); // Gán công ty vào tài khoản

        // Giả lập repository trả về tài khoản này
        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));

        // Giả lập repository trả về thành viên không tồn tại
        when(memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId("testUser", 1))
                .thenReturn(Optional.empty()); // Member không tồn tại

        // Tạo sự kiện có trạng thái SCHEDULED
        Event event = new Event();
        event.setStatus(EEventStatus.SCHEDULED);
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));

        // Tạo request list hợp lệ
        List<CreateEventActivityAndTicketRequest> requestList = List.of(
                CreateEventActivityAndTicketRequest.builder().eventId(1).build()
        );

        // Kiểm tra xem hàm có ném exception MEMBER_NOT_FOUND không
        AppException ex = assertThrows(AppException.class, () ->
                eventActivityService.createEventActivityAndTicket(requestList));

        // Kiểm tra mã lỗi trả về là MEMBER_NOT_FOUND
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, ex.getErrorCode());
    }


    @Test
    void shouldCreateEventActivityAndTicketWhenRequestListIsValid() {
        // Tạo role MANAGER
        Role role = new Role();
        role.setRoleName(ERole.MANAGER);

        // Tạo công ty
        Company company = new Company();
        company.setCompanyId(1);

        // Tạo account
        Account account = new Account();
        account.setUserName("testUser");
        account.setRole(role);
        account.setCompany(company);

        // Tạo member có quyền
        Member member = new Member();
        member.setAccount(account);
        member.setCompany(company);
        member.setSubRole(ESubRole.OWNER); // ✅ Phải có quyền OWNER hoặc ADMIN

        // Tạo event
        Event event = new Event();
        event.setStatus(EEventStatus.SCHEDULED);

        // Request
        List<CreateEventActivityAndTicketRequest> requestList = List.of(
                CreateEventActivityAndTicketRequest.builder().eventId(1).build()
        );

        // Stub dữ liệu
        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId("testUser", 1))
                .thenReturn(Optional.of(member));
        when(eventActivityRepository.save(any())).thenReturn(new EventActivity());
        when(ticketRepository.save(any())).thenReturn(new Ticket());

        // Kiểm tra không ném exception
        assertDoesNotThrow(() -> eventActivityService.createEventActivityAndTicket(requestList));
    }

//    @Test
//    void shouldCreateTicketsAndMappingsFromRequest() {
//        // Mock account có quyền
//        Role role = new Role();
//        role.setRoleName(ERole.MANAGER);
//        Company company = new Company();
//        company.setCompanyId(1);
//        Account account = new Account();
//        account.setUserName("testUser");
//        account.setRole(role);
//        account.setCompany(company);
//
//        Member member = new Member();
//        member.setAccount(account);
//        member.setCompany(company);
//        member.setSubRole(ESubRole.OWNER);
//
//        Event event = new Event();
//        event.setStatus(EEventStatus.SCHEDULED);
//
//        // Tạo request với 2 loại vé, tổng cộng 5 vé
//        CreateEventActivityAndTicketRequest.TicketRequest ticket1 = CreateEventActivityAndTicketRequest.TicketRequest.builder()
//                .ticketName("Vé VIP")
//                .ticketCode("VIP")
//                .quantity(2)
//                .price(500.0)
//                .minQuantity(1)
//                .maxQuantity(2)
//                .eventId(1)
//                .build();
//
//        CreateEventActivityAndTicketRequest.TicketRequest ticket2 = CreateEventActivityAndTicketRequest.TicketRequest.builder()
//                .ticketName("Vé Thường")
//                .ticketCode("STD")
//                .quantity(3)
//                .price(200.0)
//                .minQuantity(1)
//                .maxQuantity(5)
//                .eventId(1)
//                .build();
//
//        CreateEventActivityAndTicketRequest request = CreateEventActivityAndTicketRequest.builder()
//                .eventId(1)
//                .activityName("Đêm nhạc Rock")
//                .tickets(List.of(ticket1, ticket2))
//                .build();
//
//        // Mock repository
//        when(accountRepository.findAccountByUserName("testUser")).thenReturn(Optional.of(account));
//        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
//        when(memberRepository.findMemberByAccount_UserNameAndCompany_CompanyId("testUser", 1))
//                .thenReturn(Optional.of(member));
//        when(eventActivityRepository.save(any())).thenReturn(new EventActivity());
//        when(ticketRepository.save(any())).thenReturn(new Ticket());
//        when(ticketMappingRepository.saveAll(any())).thenReturn(List.of());
//
//        // Gọi hàm
//        eventActivityService.createEventActivityAndTicket(List.of(request));
//
//        // Tổng cộng có 5 mappings (2 + 3)
//        verify(ticketMappingRepository, times(1)).saveAll(argThat((ArgumentMatcher<List<TicketMapping>>) mappings -> mappings != null && mappings.size() == 5));
//        verify(ticketRepository, times(2)).save(any()); // 2 loại vé
//    }

}
