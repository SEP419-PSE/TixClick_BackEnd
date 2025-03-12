package com.pse.tixclick.service.impl;

import com.pse.tixclick.cloudinary.CloudinaryService;
import com.pse.tixclick.payload.dto.UpcomingEventDTO;
import com.pse.tixclick.payload.response.EventResponse;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.utils.AppUtils;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.EventDTO;
import com.pse.tixclick.payload.entity.entity_enum.ECompanyStatus;
import com.pse.tixclick.payload.entity.entity_enum.EEventStatus;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.request.create.CreateEventRequest;
import com.pse.tixclick.payload.request.update.UpdateEventRequest;
import com.pse.tixclick.service.EventService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class EventServiceImpl implements EventService {
    EventRepository eventRepository;
    ModelMapper modelMapper;
    EventCategoryRepository eventCategoryRepository;
    AccountRepository accountRepository;
    CloudinaryService cloudinary;
    CompanyRepository companyRepository;
    @Autowired
    AppUtils appUtils;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    TicketPurchaseRepository ticketPurchaseRepository;

    @Override
    public EventDTO createEvent(CreateEventRequest request, MultipartFile logoURL, MultipartFile bannerURL) throws IOException {
        if (request == null || request.getEventName() == null || request.getCategoryId() == 0) {
            throw new AppException(ErrorCode.INVALID_EVENT_DATA);
        }
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        var organnizer = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        var category = eventCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        var company = companyRepository.findCompanyByCompanyIdAndRepresentativeId_UserName(request.getCompanyId(), name)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_CREATE_COMPANY));
        if (company.getStatus() != ECompanyStatus.ACTIVE) {
            throw new AppException(ErrorCode.COMPANY_NOT_ACTIVE);
        } else if (company.getRepresentativeId().getAccountId() != organnizer.getAccountId()) {
            throw new AppException(ErrorCode.INVALID_COMPANY);
        }
        // Upload từng ảnh lên Cloudinary
        String logocode = cloudinary.uploadImageToCloudinary(logoURL);
        String bannercode = cloudinary.uploadImageToCloudinary(bannerURL);
        // Tạo đối tượng Event từ request
        Event event = new Event();
        event.setEventName(request.getEventName());
        event.setLocation(request.getLocation());
        event.setTypeEvent(request.getTypeEvent());
        event.setDescription(request.getDescription());
        event.setCategory(category);
        event.setLocationName(request.getLocationName());
        event.setStatus(EEventStatus.PENDING);
        event.setLogoURL(logocode);
        event.setBannerURL(bannercode);
        event.setOrganizer(organnizer);
        event.setCompany(company);


        // Lưu vào database
        event = eventRepository.save(event);


        // Chuyển đổi sang DTO để trả về
        return modelMapper.map(event, EventDTO.class);
    }

    @Override
    public EventDTO updateEvent(UpdateEventRequest eventRequest, MultipartFile logoURL, MultipartFile bannerURL) throws IOException {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        var event = eventRepository.findEventByEventIdAndOrganizer_UserName(eventRequest.getEventId(), name)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // Chỉ cập nhật nếu giá trị không null hoặc không phải chuỗi trống
        if (eventRequest.getEventName() != null && !eventRequest.getEventName().trim().isEmpty()) {
            event.setEventName(eventRequest.getEventName());
        }

        if (eventRequest.getDescription() != null && !eventRequest.getDescription().trim().isEmpty()) {
            event.setDescription(eventRequest.getDescription());
        }

        if (eventRequest.getLocation() != null) {
            event.setLocation(eventRequest.getLocation());
        }

        if (eventRequest.getStatus() != null) {
            event.setStatus(EEventStatus.valueOf(eventRequest.getStatus()));
        }

        if (eventRequest.getTypeEvent() != null) {
            event.setTypeEvent(eventRequest.getTypeEvent());
        }

        if (eventRequest.getCategoryId() != 0) {
            var category = eventCategoryRepository.findById(eventRequest.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            event.setCategory(category);
        }
        if (eventRequest.getLocationName() != null && !eventRequest.getLocationName().trim().isEmpty()) {
            event.setLocationName(eventRequest.getLocationName());
        }

        // Xử lý upload file nếu có
        if (logoURL != null && !logoURL.isEmpty()) {
            String logoUrl = cloudinary.uploadImageToCloudinary(logoURL);
            event.setLogoURL(logoUrl);
        }

        if (bannerURL != null && !bannerURL.isEmpty()) {
            String bannerUrl = cloudinary.uploadImageToCloudinary(bannerURL);
            event.setBannerURL(bannerUrl);
        }
        // Lưu thay đổi vào database
        event = eventRepository.save(event);

        // Chuyển đổi sang DTO và trả về
        return modelMapper.map(event, EventDTO.class);
    }

    @Override
    public boolean deleteEvent(int id) {
        var event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        event.setStatus(EEventStatus.CANCELLED);
        return true;
    }

    @Override
    public List<EventResponse> getAllEvent() {
        List<Event> events = eventRepository.findAll();
        return events.stream().map(event -> {
            EventResponse response = new EventResponse();
            response.setEventId(event.getEventId());
            response.setEventName(event.getEventName());
            response.setLocation(event.getLocation());
            response.setStatus(String.valueOf(event.getStatus()));
            response.setTypeEvent(event.getTypeEvent());

            if (event.getOrganizer() != null) {
                response.setOrganizerId(event.getOrganizer().getAccountId());
                response.setOrganizerName(event.getOrganizer().getUserName());
            }

            if (event.getCompany() != null) {
                response.setCompanyId(event.getCompany().getCompanyId());
                response.setCompanyName(event.getCompany().getCompanyName());
            }

            return response;
        }).collect(Collectors.toList());
    }


    @Override
    public EventDTO getEventById(int id) {
        var event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(event, EventDTO.class);
    }

    @Override
    public List<EventDTO> getEventByStatus(String status) {
        List<Event> events = eventRepository.findEventsByStatus(status)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {
        }.getType());
    }

    @Override
    public List<EventDTO> getEventByDraft() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        var account = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        List<Event> events = eventRepository.findEventsByStatusAndOrganizer_UserName("DRAFT", name)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {
        }.getType());
    }

    @Override
    public List<EventDTO> getEventByCompleted() {
        List<Event> events = eventRepository.findEventsByStatus("COMPLETED")
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {
        }.getType());
    }

    @Override
    public EventDTO approveEvent(int id, EEventStatus status) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        var account = accountRepository.findAccountByUserName(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (!account.getRole().getRoleName().equals("MANAGER")) {
            throw new AppException(ErrorCode.INVALID_ROLE);
        }

        var event = eventRepository.findEventByEventId(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        event.setStatus(status);
        eventRepository.save(event);
        return modelMapper.map(event, EventDTO.class);
    }

    @Override
    public List<EventDTO> getAllEventsByAccountId() {
        int uId = appUtils.getAccountFromAuthentication().getAccountId();
        List<Event> events = eventRepository.findEventByOrganizerId(uId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {
        }.getType());
    }

    @Override
    public List<EventDTO> getEventsByAccountIdAndStatus(String status) {
        int uId = appUtils.getAccountFromAuthentication().getAccountId();
        List<Event> events = eventRepository.findEventByOrganizerIdAndStatus(uId, status)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {
        }.getType());
    }

    @Override
    public List<EventDTO> getEventsByCompanyId(int companyId) {


        List<Event> events = eventRepository.findEventsByCompany_CompanyId(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {
        }.getType());

    }

    @Override
    public int countTotalScheduledEvents() {
        return Optional.of(eventRepository.countTotalScheduledEvents()).orElse(0);
    }

    @Override
    public double getAverageTicketPrice() {
        Double sum = eventRepository.getAverageTicketPrice();
        return sum == null ? 0 : sum;
    }

    @Override
    public Map<String, Double> getEventCategoryDistribution() {
        List<Object[]> results = eventRepository.getEventCategoryDistribution();
        Map<String, Double> distributionMap = new HashMap<>();

        for (Object[] result : results) {
            distributionMap.put((String) result[0], ((Number) result[1]).doubleValue());
        }
        return distributionMap;
    }

    @Override
    public List<UpcomingEventDTO> getUpcomingEvents() {
        List<Event> events = eventRepository.findScheduledEvents();
        List<UpcomingEventDTO> upcomingEventDTOs = new ArrayList<>();

        for (Event event : events) {
            int eventId = event.getEventId();

            int totalTicketsSold = ticketPurchaseRepository.countTotalTicketSold(eventId);

            double totalRevenue = orderRepository.sumTotalTransaction(eventId);

            UpcomingEventDTO dto = new UpcomingEventDTO();
            dto.setEventName(event.getEventName());
            dto.setTicketSold(totalTicketsSold);
            dto.setRevenue(totalRevenue);

            upcomingEventDTOs.add(dto);
        }

        return upcomingEventDTOs;
    }

    @Override
    public List<UpcomingEventDTO> getTopPerformingEvents() {

            List<Event> events = eventRepository.findScheduledEvents();
            List<UpcomingEventDTO> upcomingEventDTOs = new ArrayList<>();

            for (Event event : events) {
                int eventId = event.getEventId();

                int totalTicketsSold = ticketPurchaseRepository.countTotalTicketSold(eventId);

                double totalRevenue = orderRepository.sumTotalTransaction(eventId);

                UpcomingEventDTO dto = new UpcomingEventDTO();
                dto.setEventName(event.getEventName());
                dto.setTicketSold(totalTicketsSold);
                dto.setRevenue(totalRevenue);

                upcomingEventDTOs.add(dto);
            }
            // Sắp xếp danh sách theo revenue giảm dần
            upcomingEventDTOs.sort((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()));

            return upcomingEventDTOs;
    }
}
