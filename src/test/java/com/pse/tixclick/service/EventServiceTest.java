package com.pse.tixclick.service;

import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.impl.EventServiceImpl;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventCategory;
import com.pse.tixclick.payload.entity.entity_enum.ECompanyStatus;
import com.pse.tixclick.payload.entity.entity_enum.EEventStatus;
import com.pse.tixclick.payload.request.create.CreateEventRequest;
import com.pse.tixclick.cloudinary.CloudinaryService;
import com.pse.tixclick.utils.AppUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @InjectMocks
    private EventServiceImpl eventServiceImpl;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventCategoryRepository eventCategoryRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CloudinaryService cloudinary;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AppUtils appUtils;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    public void setUp() {
        // Setup the mock SecurityContext for testing
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void createEvent_ShouldThrowAppException_WhenEventNameIsNull() throws IOException {
        // Given
        CreateEventRequest request = new CreateEventRequest();
        request.setEventName(null);
        request.setCategoryId(1);
        request.setCompanyId(1);

        MultipartFile logo = mock(MultipartFile.class);
        MultipartFile banner = mock(MultipartFile.class);

        // When / Then
        AppException exception = assertThrows(AppException.class,
                () -> eventServiceImpl.createEvent(request, logo, banner));
        assertEquals("INVALID_EVENT_DATA", exception.getErrorCode().name());
    }

    @Test
    public void createEvent_ShouldReturnEventDTO_WhenValidRequest() throws IOException {
        // Given
        String eventName = "Test Event";
        CreateEventRequest request = new CreateEventRequest();
        request.setEventName(eventName);
        request.setCategoryId(1);
        request.setCompanyId(1);
        request.setTypeEvent("ONLINE");
        request.setDescription("Some description");
        request.setUrlOnline("http://example.com/live");

        // Mock the SecurityContext
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Stub Account lookup
        Account account = new Account();
        account.setAccountId(1);
        when(accountRepository.findAccountByUserName("user"))
                .thenReturn(Optional.of(account));

        // Stub Category lookup – make sure to set a non-null name!
        EventCategory eventCategory = new EventCategory();
        eventCategory.setEventCategoryId(1);
        eventCategory.setCategoryName("Music");        // <-- Prevent NPE here
        when(eventCategoryRepository.findById(1))
                .thenReturn(Optional.of(eventCategory));

        // Stub Company lookup
        Company company = new Company();
        company.setCompanyId(1);
        company.setStatus(ECompanyStatus.ACTIVE);
        company.setRepresentativeId(account);
        when(companyRepository.findCompanyByCompanyIdAndRepresentativeId_UserName(1, "user"))
                .thenReturn(Optional.of(company));

        // Stub Cloudinary uploads
        MultipartFile logo = mock(MultipartFile.class);
        MultipartFile banner = mock(MultipartFile.class);
        when(cloudinary.uploadImageToCloudinary(logo)).thenReturn("logoUrl");
        when(cloudinary.uploadImageToCloudinary(banner)).thenReturn("bannerUrl");

        // Stub save + mapping
        Event savedEvent = new Event();
        savedEvent.setEventId(1);
        savedEvent.setEventName(eventName);
        savedEvent.setStatus(EEventStatus.DRAFT);
        savedEvent.setLogoURL("logoUrl");
        savedEvent.setBannerURL("bannerUrl");
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        EventDTO dto = new EventDTO();
        dto.setEventId(1);
        when(modelMapper.map(savedEvent, EventDTO.class)).thenReturn(dto);

        // When
        EventDTO eventDTO = eventServiceImpl.createEvent(request, logo, banner);

        // Then
        assertNotNull(eventDTO);
        assertEquals(1, eventDTO.getEventId());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    public void createEvent_ShouldSetOfflineFields_WhenTypeIsOffline() throws IOException {
        // Arrange
        CreateEventRequest request = new CreateEventRequest();
        request.setEventName("Offline Event");
        request.setCategoryId(2);
        request.setCompanyId(3);
        request.setTypeEvent("offline");                 // lowercase to test toUpperCase()
        request.setLocationName("Conference Hall");
        request.setAddress("123 Main St");
        request.setWard("Ward 5");
        request.setDistrict("District 1");
        request.setCity("Ho Chi Minh");

        // Mock SecurityContext → Authentication → username "bob"
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("bob");
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        // Stub account lookup
        Account bob = new Account();
        bob.setAccountId(10);
        when(accountRepository.findAccountByUserName("bob")).thenReturn(Optional.of(bob));

        // Stub category lookup (with non-null name to avoid NPE in toUpperCase())
        EventCategory cat = new EventCategory();
        cat.setEventCategoryId(2);
        cat.setCategoryName("Art");
        when(eventCategoryRepository.findById(2)).thenReturn(Optional.of(cat));

        // Stub company lookup (active, correct rep)
        Company comp = new Company();
        comp.setCompanyId(3);
        comp.setStatus(ECompanyStatus.ACTIVE);
        comp.setRepresentativeId(bob);
        when(companyRepository
                .findCompanyByCompanyIdAndRepresentativeId_UserName(3, "bob"))
                .thenReturn(Optional.of(comp));

        // Stub Cloudinary uploads
        MultipartFile logo = mock(MultipartFile.class);
        MultipartFile banner = mock(MultipartFile.class);
        when(cloudinary.uploadImageToCloudinary(logo)).thenReturn("logoUrl");
        when(cloudinary.uploadImageToCloudinary(banner)).thenReturn("bannerUrl");

        // Capture what gets saved
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);

        // Stub save → return some Event to map
        Event saved = new Event();
        saved.setEventId(42);
        when(eventRepository.save(captor.capture())).thenReturn(saved);

        EventDTO dto = new EventDTO();
        dto.setEventId(42);
        when(modelMapper.map(saved, EventDTO.class)).thenReturn(dto);

        // Act
        EventDTO result = eventServiceImpl.createEvent(request, logo, banner);

        // Assert DTO
        assertNotNull(result);
        assertEquals(42, result.getEventId());

        // Assert that repository.save received an Event with offline fields
        Event captured = captor.getValue();
        assertNull(captured.getUrlOnline(), "urlOnline must be null for OFFLINE");
        assertEquals("Conference Hall", captured.getLocationName());
        assertEquals("123 Main St",       captured.getAddress());
        assertEquals("Ward 5",            captured.getWard());
        assertEquals("District 1",        captured.getDistrict());
        assertEquals("Ho Chi Minh",       captured.getCity());

        // Also verify common fields were set
        assertEquals(EEventStatus.DRAFT, captured.getStatus());
        verify(cloudinary).uploadImageToCloudinary(logo);
        verify(cloudinary).uploadImageToCloudinary(banner);
        verify(eventRepository).save(any(Event.class));
        verify(modelMapper).map(saved, EventDTO.class);
    }

    @Test
    public void createEvent_ShouldThrowException_WhenCompanyIsNotActive() throws IOException {
        // Arrange
        CreateEventRequest request = new CreateEventRequest();
        request.setEventName("Test Event");
        request.setCategoryId(1);
        request.setCompanyId(1);
        request.setTypeEvent("ONLINE");

        // Mock SecurityContext → Authentication → username "bob"
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("bob");
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        // Stub account lookup
        Account bob = new Account();
        bob.setAccountId(10);
        when(accountRepository.findAccountByUserName("bob")).thenReturn(Optional.of(bob));

        // Stub category lookup
        EventCategory cat = new EventCategory();
        cat.setEventCategoryId(1);
        cat.setCategoryName("Music");
        when(eventCategoryRepository.findById(1)).thenReturn(Optional.of(cat));

        // Stub company lookup with non-active status
        Company company = new Company();
        company.setCompanyId(1);
        company.setStatus(ECompanyStatus.INACTIVE);  // Setting status as INACTIVE
        company.setRepresentativeId(bob);
        when(companyRepository.findCompanyByCompanyIdAndRepresentativeId_UserName(1, "bob"))
                .thenReturn(Optional.of(company));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            eventServiceImpl.createEvent(request, null, null);
        });
        assertEquals(ErrorCode.COMPANY_NOT_ACTIVE, exception.getErrorCode());
    }
    @Test
    public void createEvent_ShouldThrowException_WhenRepresentativeIsNotOrganizer() throws IOException {
        // Arrange
        CreateEventRequest request = new CreateEventRequest();
        request.setEventName("Test Event");
        request.setCategoryId(1);
        request.setCompanyId(1);
        request.setTypeEvent("ONLINE");

        // Mock SecurityContext → Authentication → username "bob"
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("bob");
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        // Stub account lookup for the actual organizer
        Account bob = new Account();
        bob.setAccountId(10);
        when(accountRepository.findAccountByUserName("bob")).thenReturn(Optional.of(bob));

        // Stub account lookup for another user (not the representative)
        Account alice = new Account();
        alice.setAccountId(20);  // Different accountId from bob

        // Stub category lookup
        EventCategory cat = new EventCategory();
        cat.setEventCategoryId(1);
        cat.setCategoryName("Music");
        when(eventCategoryRepository.findById(1)).thenReturn(Optional.of(cat));

        // Stub company lookup with representative different from organizer
        Company company = new Company();
        company.setCompanyId(1);
        company.setStatus(ECompanyStatus.ACTIVE);  // Status is active
        company.setRepresentativeId(alice);      // Representative is Alice, not Bob
        when(companyRepository.findCompanyByCompanyIdAndRepresentativeId_UserName(1, "bob"))
                .thenReturn(Optional.of(company));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            eventServiceImpl.createEvent(request, null, null);
        });
        assertEquals(ErrorCode.INVALID_COMPANY, exception.getErrorCode());
    }
    @Test
    public void createEvent_ShouldThrowIllegalArgumentException_WhenTypeEventInvalid() throws IOException {
        // Arrange
        CreateEventRequest request = new CreateEventRequest();
        request.setEventName("Bad Type Event");
        request.setCategoryId(1);
        request.setCompanyId(1);
        request.setTypeEvent("HYBRID");   // not ONLINE or OFFLINE

        // Mock SecurityContext → Authentication → username "bob"
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("bob");
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        // Stub account lookup
        Account bob = new Account();
        bob.setAccountId(10);
        when(accountRepository.findAccountByUserName("bob"))
                .thenReturn(Optional.of(bob));

        // Stub category lookup (name non‑null to avoid NPE upstream)
        EventCategory cat = new EventCategory();
        cat.setEventCategoryId(1);
        cat.setCategoryName("Music");
        when(eventCategoryRepository.findById(1))
                .thenReturn(Optional.of(cat));

        // Stub company lookup (active + correct rep)
        Company comp = new Company();
        comp.setCompanyId(1);
        comp.setStatus(ECompanyStatus.ACTIVE);
        comp.setRepresentativeId(bob);
        when(companyRepository
                .findCompanyByCompanyIdAndRepresentativeId_UserName(1, "bob"))
                .thenReturn(Optional.of(comp));

        // Stub the image uploads (these happen before the type check)
        MultipartFile logo = mock(MultipartFile.class);
        MultipartFile banner = mock(MultipartFile.class);
        when(cloudinary.uploadImageToCloudinary(logo)).thenReturn("logoUrl");
        when(cloudinary.uploadImageToCloudinary(banner)).thenReturn("bannerUrl");

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> eventServiceImpl.createEvent(request, logo, banner)
        );
        assertEquals(
                "Invalid event type. Must be ONLINE or OFFLINE.",
                ex.getMessage()
        );
    }

}
