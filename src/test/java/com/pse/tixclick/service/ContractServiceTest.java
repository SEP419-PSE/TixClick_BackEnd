package com.pse.tixclick.service;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Role;
import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.company.Contract;
import com.pse.tixclick.payload.entity.company.ContractDetail;
import com.pse.tixclick.payload.entity.entity_enum.*;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.payment.ContractPayment;
import com.pse.tixclick.payload.entity.seatmap.*;
import com.pse.tixclick.payload.request.create.ContractDetailRequest;
import com.pse.tixclick.payload.request.create.CreateContractAndDetailRequest;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.impl.ContractServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
 class ContractServiceTest {
    @InjectMocks
    private ContractServiceImpl contractService;

    @Mock private ContractRepository contractRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private EventRepository eventRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private ContractDetailRepository contractDetailRepository;
    @Mock private ContractPaymentRepository contractPaymentRepository;
    @Mock private EventActivityRepository eventActivityRepository;
    @Mock private ZoneRepository zoneRepository;
    @Mock private ZoneActivityRepository zoneActivityRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private SeatActivityRepository seatActivityRepository;
    @Mock private TicketMappingRepository ticketMappingRepository;
    @Mock private ContractDocumentService contractDocumentService;

    private MultipartFile dummyPdf;

    @BeforeEach
    void setUp() {
        dummyPdf = new MockMultipartFile("file", "contract.pdf", "application/pdf", new byte[] {0});
        // Spy the service so we can stub out extractTextFromPdf() and parse(...)
        contractService = Mockito.spy(contractService);
    }

    @Test
    void createContract_ShouldThrowScanningFailed_WhenParseReturnsNull() throws IOException {
        doReturn("some text").when(contractService).extractTextFromPdf(dummyPdf);
        doReturn(null).when(contractService).parse("some text");

        AppException ex = assertThrows(AppException.class, () ->
                contractService.createContractAndContractDetail(dummyPdf));
        assertEquals(ErrorCode.CONTRACT_SCANNING_FAILED, ex.getErrorCode());
    }

    @Test
    void createContract_ShouldThrowExisted_WhenContractCodeAlreadyExists() throws IOException {
        CreateContractAndDetailRequest req = new CreateContractAndDetailRequest();
        req.setContractCode("C123");
        doReturn("pdf").when(contractService).extractTextFromPdf(dummyPdf);
        doReturn(req).when(contractService).parse("pdf");

        when(contractRepository.findByContractCode("C123"))
                .thenReturn(new Contract());

        AppException ex = assertThrows(AppException.class, () ->
                contractService.createContractAndContractDetail(dummyPdf));
        assertEquals(ErrorCode.CONTRACT_EXISTED, ex.getErrorCode());
    }

    @Test
    void createContract_ShouldThrowUserNotFound_WhenEmailANotExist() throws IOException {
        CreateContractAndDetailRequest req = new CreateContractAndDetailRequest();
        req.setContractCode("C1");
        req.setEmailA("a@example.com");
        doReturn("pdf").when(contractService).extractTextFromPdf(dummyPdf);
        doReturn(req).when(contractService).parse("pdf");

        when(contractRepository.findByContractCode("C1")).thenReturn(null);
        when(accountRepository.findAccountByEmail("a@example.com"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                contractService.createContractAndContractDetail(dummyPdf));
        assertEquals(ErrorCode.USER_NOT_EXISTED, ex.getErrorCode());
    }


    @Test
    void createContract_ShouldThrowUserNotManager_WhenRoleWrong() throws IOException {
        CreateContractAndDetailRequest req = new CreateContractAndDetailRequest();
        req.setContractCode("C2");
        req.setEmailA("b@example.com");
        doReturn("pdf").when(contractService).extractTextFromPdf(dummyPdf);
        doReturn(req).when(contractService).parse("pdf");

        when(contractRepository.findByContractCode("C2")).thenReturn(null);
        Account user = new Account();
        Role r = new Role(); r.setRoleName(ERole.BUYER);
        user.setRole(r);
        when(accountRepository.findAccountByEmail("b@example.com"))
                .thenReturn(Optional.of(user));

        AppException ex = assertThrows(AppException.class, () ->
                contractService.createContractAndContractDetail(dummyPdf));
        assertEquals(ErrorCode.USER_NOT_MANAGER, ex.getErrorCode());
    }

    @Test
    void createContract_ShouldThrowEventNotFound_WhenEventMissing() throws IOException {
        CreateContractAndDetailRequest req = new CreateContractAndDetailRequest();
        req.setContractCode("C3");
        req.setEmailA("m@ex"); req.setEventCode("E123");
        doReturn("t").when(contractService).extractTextFromPdf(dummyPdf);
        doReturn(req).when(contractService).parse("t");

        when(contractRepository.findByContractCode("C3")).thenReturn(null);
        Account m = new Account(); Role rm = new Role(); rm.setRoleName(ERole.MANAGER); m.setRole(rm);
        when(accountRepository.findAccountByEmail("m@ex"))
                .thenReturn(Optional.of(m));
        when(eventRepository.findEventByEventCode("E123"))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                contractService.createContractAndContractDetail(dummyPdf));
        assertEquals(ErrorCode.EVENT_NOT_FOUND, ex.getErrorCode());
    }
    @Test
    void createContract_ShouldThrowEventScheduled_WhenAlreadyScheduled() throws IOException {
        CreateContractAndDetailRequest req = new CreateContractAndDetailRequest();
        req.setContractCode("C4"); req.setEmailA("x@ex"); req.setEventCode("E4");
        doReturn("t").when(contractService).extractTextFromPdf(dummyPdf);
        doReturn(req).when(contractService).parse("t");

        when(contractRepository.findByContractCode("C4")).thenReturn(null);
        Account m = new Account(); Role rm = new Role(); rm.setRoleName(ERole.MANAGER); m.setRole(rm);
        when(accountRepository.findAccountByEmail("x@ex"))
                .thenReturn(Optional.of(m));

        Event e = new Event();
        e.setStatus(EEventStatus.SCHEDULED);
        when(eventRepository.findEventByEventCode("E4")).thenReturn(Optional.of(e));

        AppException ex = assertThrows(AppException.class, () ->
                contractService.createContractAndContractDetail(dummyPdf));
        assertEquals(ErrorCode.EVENT_SCHEDULED, ex.getErrorCode());
    }

    @Test
    void createContract_ShouldThrowEventNotApproved_WhenWrongStatus() throws IOException {
        CreateContractAndDetailRequest req = new CreateContractAndDetailRequest();
        req.setContractCode("C5"); req.setEmailA("y@ex"); req.setEventCode("E5");
        doReturn("t").when(contractService).extractTextFromPdf(dummyPdf);
        doReturn(req).when(contractService).parse("t");

        when(contractRepository.findByContractCode("C5")).thenReturn(null);
        Account m = new Account(); Role rm = new Role(); rm.setRoleName(ERole.MANAGER); m.setRole(rm);
        when(accountRepository.findAccountByEmail("y@ex"))
                .thenReturn(Optional.of(m));

        Event e = new Event();
        e.setStatus(EEventStatus.DRAFT);
        when(eventRepository.findEventByEventCode("E5")).thenReturn(Optional.of(e));

        AppException ex = assertThrows(AppException.class, () ->
                contractService.createContractAndContractDetail(dummyPdf));
        assertEquals(ErrorCode.EVENT_NOT_APPROVED, ex.getErrorCode());
    }


//    @Test
//    void createContract_ShouldThrowCompanyNotFound_WhenEmailBNotExist() throws IOException {
//        CreateContractAndDetailRequest req = new CreateContractAndDetailRequest();
//        req.setContractCode("C6"); req.setEmailA("mgr@ex"); req.setEventCode("E6"); req.setEmailB("biz@ex");
//        doReturn("t").when(contractService).extractTextFromPdf(dummyPdf);
//        doReturn(req).when(contractService).parse("t");
//
//        when(contractRepository.findByContractCode("C6")).thenReturn(null);
//        Account m = new Account(); Role rm = new Role(); rm.setRoleName(ERole.MANAGER); m.setRole(rm);
//        when(accountRepository.findAccountByEmail("mgr@ex"))
//                .thenReturn(Optional.of(m));
//        Event e = new Event(); e.setStatus(EEventStatus.APPROVED);
//        when(eventRepository.findEventByEventCode("E6")).thenReturn(Optional.of(e));
//
//        when(companyRepository.findCompanyByEmail("biz@ex")).thenReturn(Optional.empty());
//
//        AppException ex = assertThrows(AppException.class, () ->
//                contractService.createContractAndContractDetail(dummyPdf));
//        assertEquals(ErrorCode.COMPANY_NOT_FOUND, ex.getErrorCode());
//    }
//
//    @Test
//    void createContract_ShouldThrowEventNotCompany_WhenCompanyMismatch() throws IOException {
//        CreateContractAndDetailRequest req = new CreateContractAndDetailRequest();
//        req.setContractCode("C7"); req.setEmailA("m@ex"); req.setEventCode("E7"); req.setEmailB("biz@ex");
//        doReturn("t").when(contractService).extractTextFromPdf(dummyPdf);
//        doReturn(req).when(contractService).parse("t");
//
//        when(contractRepository.findByContractCode("C7")).thenReturn(null);
//        Account m = new Account(); Role rm = new Role(); rm.setRoleName(ERole.MANAGER); m.setRole(rm);
//        when(accountRepository.findAccountByEmail("m@ex"))
//                .thenReturn(Optional.of(m));
//        Event e = new Event(); e.setStatus(EEventStatus.APPROVED);
//        Company c = new Company(); c.setCompanyId(99);
//        e.setCompany(c);
//        when(eventRepository.findEventByEventCode("E7")).thenReturn(Optional.of(e));
//        Company other = new Company(); other.setCompanyId(100);
//        when(companyRepository.findCompanyByEmail("biz@ex"))
//                .thenReturn(Optional.of(other));
//
//        AppException ex = assertThrows(AppException.class, () ->
//                contractService.createContractAndContractDetail(dummyPdf));
//        assertEquals(ErrorCode.EVENT_NOT_COMPANY, ex.getErrorCode());
//    }
//
//
//    @Test
//    void createContract_ShouldSucceed_WhenEverythingValid() throws Exception {
//        // --- build a minimal valid request ---
//        CreateContractAndDetailRequest req = new CreateContractAndDetailRequest();
//        req.setContractCode("C8");
//        req.setEmailA("m@ex");
//        req.setEventCode("E8");
//        req.setEmailB("biz@ex");
//        req.setContractName("CN");
//        req.setContractValue(1000.0);
//        req.setCommission("10%");
//        req.setContractType(EContractType.ONE_TIME.name());
//        req.setContractStartDate(LocalDate.now());
//        req.setContractEndDate(LocalDate.now().plusDays(1));
//
//        ContractDetailRequest dt = new ContractDetailRequest();
//        dt.setContractDetailName("O");
//        dt.setContractDetailCode("C8");
//        dt.setContractDetailDescription("D");
//        dt.setContractDetailAmount(500.0);
//        dt.setContractDetailPercentage(100.0);
//        dt.setContractDetailPayDate(LocalDate.now());
//        req.setContractDetails(List.of(dt));
//
//        // spy out PDF extraction & parsing
//        doReturn("t").when(contractService).extractTextFromPdf(dummyPdf);
//        doReturn(req).when(contractService).parse("t");
//
//        // repo stubs
//        when(contractRepository.findByContractCode("C8")).thenReturn(null);
//        Account mgr = new Account();
//        Role rm = new Role();
//        rm.setRoleName(ERole.MANAGER);
//        mgr.setRole(rm);
//        when(accountRepository.findAccountByEmail("m@ex")).thenReturn(Optional.of(mgr));
//
//        Event e = new Event();
//        e.setStatus(EEventStatus.APPROVED);
//        Company c = new Company();
//        c.setCompanyId(50);
//        e.setCompany(c);
//        SeatMap sm = new SeatMap();
//        sm.setSeatMapId(5);
//        e.setSeatMap(sm);
//        when(eventRepository.findEventByEventCode("E8")).thenReturn(Optional.of(e));
//        when(companyRepository.findCompanyByEmail("biz@ex")).thenReturn(Optional.of(c));
//
//        // one EventActivity
//        EventActivity act = new EventActivity();
//        act.setEventActivityId(1);
//        act.setEvent(e);
//        when(eventActivityRepository.findEventActivitiesByEvent_EventId(e.getEventId()))
//                .thenReturn(List.of(act));
//
//        // use a STANDING zone
//        Zone z = new Zone();
//        z.setZoneId(1);
//        ZoneType zoneType = new ZoneType();
//        zoneType.setTypeName(ZoneTypeEnum.STANDING);
//        z.setZoneType(zoneType);
//        z.setQuantity(10);
//        when(zoneRepository.findBySeatMapId(5)).thenReturn(List.of(z));
//
//        // capture the ZoneActivity save
//        ZoneActivity savedZA = new ZoneActivity();
//        savedZA.setZoneActivityId(99);
//        when(zoneActivityRepository.save(any(ZoneActivity.class))).thenReturn(savedZA);
//
//        // other saves
//        when(contractRepository.save(any(Contract.class))).thenReturn(new Contract());
//        when(contractDetailRepository.save(any(ContractDetail.class))).thenReturn(new ContractDetail());
//        when(contractPaymentRepository.save(any(ContractPayment.class))).thenReturn(new ContractPayment());
//        when(eventActivityRepository.save(any(EventActivity.class))).thenReturn(act);
//
//        // call service
//        CreateContractAndDetailRequest returned = contractService.createContractAndContractDetail(dummyPdf);
//
//        // verify
//        assertSame(req, returned);
//        verify(eventRepository, times(2)).save(e);
//        verify(contractRepository).save(any(Contract.class));
//        verify(contractDetailRepository).save(any(ContractDetail.class));
//        verify(contractPaymentRepository).save(any(ContractPayment.class));
//        verify(zoneActivityRepository).save(any(ZoneActivity.class));
//        verify(eventActivityRepository).save(any(EventActivity.class));
//
//        // verify that seatActivity was NOT created for STANDING zone
//        verify(seatActivityRepository, never()).saveAll(anyList());
//    }
//
//    @Test
//    void createContract_ShouldThrowException_WhenSeatMapIsNullAndNoTicketMappings() throws Exception {
//        // --- build a minimal valid request ---
//        CreateContractAndDetailRequest req = new CreateContractAndDetailRequest();
//        req.setContractCode("C8");
//        req.setEmailA("m@ex");
//        req.setEventCode("E8");
//        req.setEmailB("biz@ex");
//        req.setContractName("CN");
//        req.setContractValue(1000.0);
//        req.setCommission("10%");
//        req.setContractType(EContractType.ONE_TIME.name());
//        req.setContractStartDate(LocalDate.now());
//        req.setContractEndDate(LocalDate.now().plusDays(1));
//
//        ContractDetailRequest dt = new ContractDetailRequest();
//        dt.setContractDetailName("O");
//        dt.setContractDetailCode("C8");
//        dt.setContractDetailDescription("D");
//        dt.setContractDetailAmount(500.0);
//        dt.setContractDetailPercentage(100.0);
//        dt.setContractDetailPayDate(LocalDate.now());
//        req.setContractDetails(List.of(dt));
//
//        // spy out PDF extraction & parsing
//        doReturn("t").when(contractService).extractTextFromPdf(dummyPdf);
//        doReturn(req).when(contractService).parse("t");
//
//        // repo stubs
//        when(contractRepository.findByContractCode("C8")).thenReturn(null);
//        Account mgr = new Account();
//        Role rm = new Role();
//        rm.setRoleName(ERole.MANAGER);
//        mgr.setRole(rm);
//        when(accountRepository.findAccountByEmail("m@ex")).thenReturn(Optional.of(mgr));
//
//        Event e = new Event();
//        e.setStatus(EEventStatus.APPROVED);
//        Company c = new Company();
//        c.setCompanyId(50);
//        e.setCompany(c);
//        e.setSeatMap(null); // Set seatMap to null
//        when(eventRepository.findEventByEventCode("E8")).thenReturn(Optional.of(e));
//        when(companyRepository.findCompanyByEmail("biz@ex")).thenReturn(Optional.of(c));
//
//        // Mock ticketMappingRepository to return empty list
//        when(ticketMappingRepository.findTicketMappingsByEventActivity_Event(e)).thenReturn(Collections.emptyList());
//
//        // Expect exception
//        AppException exception = assertThrows(AppException.class, () -> {
//            contractService.createContractAndContractDetail(dummyPdf);
//        });
//
//        // Verify exception
//        assertEquals(ErrorCode.EVENT_NOT_HAVE_SEATMAP, exception.getErrorCode());
//
//        // Verify interactions
//        verify(eventRepository, times(1)).save(e); // Only one save before the exception
//        verify(contractRepository).save(any(Contract.class));
//        verify(contractDetailRepository).save(any(ContractDetail.class));
//        verify(contractPaymentRepository).save(any(ContractPayment.class));
//        verify(ticketMappingRepository).findTicketMappingsByEventActivity_Event(e);
//        verify(zoneRepository, never()).findBySeatMapId((int) anyLong()); // No seatMap, so this shouldn't be called
//        verify(seatActivityRepository, never()).saveAll(anyList()); // No seat activities created
//    }
//
//    @Test
//    void createContract_ShouldCreateSeatActivities_WhenZoneIsSeated() throws Exception {
//        // --- build a minimal valid request ---
//        CreateContractAndDetailRequest req = new CreateContractAndDetailRequest();
//        req.setContractCode("C8");
//        req.setEmailA("m@ex");
//        req.setEventCode("E8");
//        req.setEmailB("biz@ex");
//        req.setContractName("CN");
//        req.setContractValue(1000.0);
//        req.setCommission("10%");
//        req.setContractType(EContractType.ONE_TIME.name());
//        req.setContractStartDate(LocalDate.now());
//        req.setContractEndDate(LocalDate.now().plusDays(1));
//
//        ContractDetailRequest dt = new ContractDetailRequest();
//        dt.setContractDetailName("O");
//        dt.setContractDetailCode("C8");
//        dt.setContractDetailDescription("D");
//        dt.setContractDetailAmount(500.0);
//        dt.setContractDetailPercentage(100.0);
//        dt.setContractDetailPayDate(LocalDate.now());
//        req.setContractDetails(List.of(dt));
//
//        // spy out PDF extraction & parsing
//        doReturn("t").when(contractService).extractTextFromPdf(dummyPdf);
//        doReturn(req).when(contractService).parse("t");
//
//        // repo stubs
//        when(contractRepository.findByContractCode("C8")).thenReturn(null);
//        Account mgr = new Account();
//        Role rm = new Role();
//        rm.setRoleName(ERole.MANAGER);
//        mgr.setRole(rm);
//        when(accountRepository.findAccountByEmail("m@ex")).thenReturn(Optional.of(mgr));
//
//        Event e = new Event();
//        e.setStatus(EEventStatus.APPROVED);
//        Company c = new Company();
//        c.setCompanyId(50);
//        e.setCompany(c);
//        SeatMap sm = new SeatMap();
//        sm.setSeatMapId(5);
//        e.setSeatMap(sm);
//        when(eventRepository.findEventByEventCode("E8")).thenReturn(Optional.of(e));
//        when(companyRepository.findCompanyByEmail("biz@ex")).thenReturn(Optional.of(c));
//
//        // one EventActivity
//        EventActivity act = new EventActivity();
//        act.setEventActivityId(1);
//        act.setEvent(e);
//        when(eventActivityRepository.findEventActivitiesByEvent_EventId(e.getEventId()))
//                .thenReturn(List.of(act));
//
//        // use a SEATED zone
//        Zone z = new Zone();
//        z.setZoneId(1);
//        ZoneType zoneType = new ZoneType();
//        zoneType.setTypeName(ZoneTypeEnum.SEATED); // Use SEATED
//        z.setZoneType(zoneType);
//        z.setQuantity(10);
//        when(zoneRepository.findBySeatMapId(5)).thenReturn(List.of(z));
//
//        // mock seats in the zone
//        Seat seat1 = new Seat();
//        seat1.setSeatId(1);
//        seat1.setZone(z);
//        Seat seat2 = new Seat();
//        seat2.setSeatId(2);
//        seat2.setZone(z);
//        when(seatRepository.findSeatsByZone_ZoneId(z.getZoneId())).thenReturn(List.of(seat1, seat2));
//
//        // capture the ZoneActivity save
//        ZoneActivity savedZA = new ZoneActivity();
//        savedZA.setZoneActivityId(99);
//        when(zoneActivityRepository.save(any(ZoneActivity.class))).thenReturn(savedZA);
//
//        // mock seatActivity save
//        SeatActivity sa1 = new SeatActivity();
//        SeatActivity sa2 = new SeatActivity();
//        when(seatActivityRepository.saveAll(anyList())).thenReturn(List.of(sa1, sa2));
//
//        // other saves
//        when(contractRepository.save(any(Contract.class))).thenReturn(new Contract());
//        when(contractDetailRepository.save(any(ContractDetail.class))).thenReturn(new ContractDetail());
//        when(contractPaymentRepository.save(any(ContractPayment.class))).thenReturn(new ContractPayment());
//        when(eventActivityRepository.save(any(EventActivity.class))).thenReturn(act);
//
//        // call service
//        CreateContractAndDetailRequest returned = contractService.createContractAndContractDetail(dummyPdf);
//
//        // verify
//        assertSame(req, returned);
//        verify(eventRepository, times(2)).save(e);
//        verify(contractRepository).save(any(Contract.class));
//        verify(contractDetailRepository).save(any(ContractDetail.class));
//        verify(contractPaymentRepository).save(any(ContractPayment.class));
//        verify(zoneActivityRepository).save(any(ZoneActivity.class));
//        verify(eventActivityRepository).save(any(EventActivity.class));
//        verify(seatRepository).findSeatsByZone_ZoneId(z.getZoneId());
//
//        // verify that seatActivities were created and saved
//        ArgumentCaptor<List<SeatActivity>> captor = ArgumentCaptor.forClass(List.class);
//        verify(seatActivityRepository).saveAll(captor.capture());
//        List<SeatActivity> savedSeatActivities = captor.getValue();
//        assertEquals(2, savedSeatActivities.size()); // Expect 2 SeatActivities
//        assertEquals(ESeatActivityStatus.AVAILABLE, savedSeatActivities.get(0).getStatus());
//        assertEquals(seat1, savedSeatActivities.get(0).getSeat());
//        assertEquals(savedZA, savedSeatActivities.get(0).getZoneActivity());
//        assertEquals(act, savedSeatActivities.get(0).getEventActivity());
//    }
}
