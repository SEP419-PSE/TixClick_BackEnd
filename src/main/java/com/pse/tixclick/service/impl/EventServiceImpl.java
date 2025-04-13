package com.pse.tixclick.service.impl;

import com.pse.tixclick.cloudinary.CloudinaryService;
import com.pse.tixclick.email.EmailService;
import com.pse.tixclick.payload.dto.EventActivityDTO;
import com.pse.tixclick.payload.dto.TicketDTO;
import com.pse.tixclick.payload.dto.UpcomingEventDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.Notification;
import com.pse.tixclick.payload.entity.company.Company;
import com.pse.tixclick.payload.entity.company.Contract;
import com.pse.tixclick.payload.entity.entity_enum.EContractStatus;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.entity.ticket.TicketMapping;
import com.pse.tixclick.payload.response.*;
import com.pse.tixclick.repository.*;
import com.pse.tixclick.service.TicketMappingService;
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
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    SimpMessagingTemplate messagingTemplate;
    ContractRepository contractRepository;
    EmailService emailService;
    TicketRepository ticketRepository;
    SeatMapRepository seatMapRepository;
    TicketMappingRepository ticketMappingRepository;
    TicketPurchaseRepository ticketPurchaseRepository;
    AppUtils appUtils;
    OrderRepository orderRepository;
    TicketMappingService ticketMappingService;
    NotificationRepository notificationRepository;



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
        String typeEvent = request.getTypeEvent().toUpperCase();

        // Kiểm tra xem typeEvent có phải là "ONLINE" hoặc "OFFLINE" không (có thể sử dụng toUpperCase() để kiểm tra không phân biệt chữ hoa chữ thường)
        if ("ONLINE".equalsIgnoreCase(typeEvent) || "OFFLINE".equalsIgnoreCase(typeEvent)) {
            // Chuyển typeEvent về chữ in hoa trước khi gán vào event
            event.setTypeEvent(typeEvent.toUpperCase());
        } else {
            // Nếu không phải ONLINE hoặc OFFLINE, có thể xử lý thêm tùy theo yêu cầu
            // Ví dụ: throw exception, log error hoặc gán một giá trị mặc định
            throw new IllegalArgumentException("Invalid event type. Must be ONLINE or OFFLINE.");
        }
        event.setTypeEvent(request.getTypeEvent());
        event.setDescription(request.getDescription());
        event.setCategory(category);

        event.setStatus(EEventStatus.DRAFT);
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLogoURL(logocode);
        event.setBannerURL(bannercode);
        event.setOrganizer(organnizer);
        event.setCountView(0);
        event.setCompany(company);
        if("ONLINE".equals(request.getTypeEvent().toUpperCase())) {
            event.setUrlOnline(request.getUrlOnline());
            event.setLocationName(null);
            event.setLocation(null);
        } else if("OFFLINE".equals(request.getTypeEvent().toUpperCase())) {
            event.setUrlOnline(null);
            event.setLocationName(request.getLocationName());
            event.setLocation(request.getLocation());
        }


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
        if(event.getStatus() != EEventStatus.DRAFT) {
            throw new AppException(ErrorCode.INVALID_EVENT_STATUS);
        }
        // Chỉ cập nhật nếu giá trị không null hoặc không phải chuỗi trống
        if (eventRequest.getEventName() != null && !eventRequest.getEventName().trim().isEmpty()) {
            event.setEventName(eventRequest.getEventName());
        }

        if (eventRequest.getDescription() != null && !eventRequest.getDescription().trim().isEmpty()) {
            event.setDescription(eventRequest.getDescription());
        }

        if(eventRequest.getStartDate() != null) {
            event.setStartDate(eventRequest.getStartDate());
        }
        if(eventRequest.getEndDate() != null) {
            event.setEndDate(eventRequest.getEndDate());
        }


        if (eventRequest.getStatus() != null ) {
            event.setStatus(EEventStatus.valueOf(eventRequest.getStatus()));
        }
        if (eventRequest.getUrlOnline() != null && !eventRequest.getUrlOnline().trim().isEmpty()) {
            if ("ONLINE".equalsIgnoreCase(event.getTypeEvent())) {
                event.setUrlOnline(eventRequest.getUrlOnline());
                event.setLocation(null);
                event.setLocationName(null);
            } else {
                // Nếu không phải ONLINE thì bỏ qua phần set urlOnline
                // Có thể log ra nếu cần debug
                System.out.println("Event type is not ONLINE, skip setting urlOnline.");
            }
        }

        if (eventRequest.getTypeEvent() != null) {
            String type = eventRequest.getTypeEvent().toUpperCase();
            if (!type.equals("ONLINE") && !type.equals("OFFLINE")) {
                throw new AppException(ErrorCode.INVALID_EVENT_TYPE);
            }
            event.setTypeEvent(type);
        }

        if (eventRequest.getCategoryId() != 0) {
            var category = eventCategoryRepository.findById(eventRequest.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            event.setCategory(category);
        }

        if(event.getTypeEvent().equals("OFFLINE")) {
            event.setUrlOnline(null);
            if (eventRequest.getLocationName() != null && !eventRequest.getLocationName().trim().isEmpty()) {
                event.setLocationName(eventRequest.getLocationName());
            }
            if (eventRequest.getLocation() != null) {
                event.setLocation(eventRequest.getLocation());
            }
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
        event.setStatus(EEventStatus.REJECTED);
        return true;
    }

    @Override
    public List<EventResponse> getAllEvent() {
        List<Event> events = eventRepository.findAll();
        return events.stream().map(event -> {
            EventResponse response = new EventResponse();
            response.setEventId(event.getEventId());
            response.setBannerURL(event.getBannerURL());
            response.setLogoURL(event.getLogoURL());
            response.setDescription(event.getDescription());
            response.setLocationName(event.getLocationName());
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
    public List<EventResponse> getAllEventScheduledAndPendingApproved() {
        List<Event> events = eventRepository.findAll();
        return events.stream()
                .filter(event -> event.getStatus() == EEventStatus.SCHEDULED || event.getStatus() == EEventStatus.PENDING)
                .map(event -> {
            EventResponse response = new EventResponse();
            response.setEventId(event.getEventId());
            response.setBannerURL(event.getBannerURL());
            response.setLogoURL(event.getLogoURL());
            response.setDescription(event.getDescription());
            response.setLocationName(event.getLocationName());
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
    public List<EventDTO> getEventByStatus(EEventStatus status) {
        List<Event> events = eventRepository.findEventsByStatus(status);
        if (events.isEmpty()) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }

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
        List<Event> events = eventRepository.findEventsByStatus(EEventStatus.valueOf("COMPLETED"));
        if (events.isEmpty()) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }
        return modelMapper.map(events, new TypeToken<List<EventDTO>>() {
        }.getType());
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
        List<Event> events = eventRepository.findEventByOrganizerIdAndStatus(uId, EEventStatus.valueOf(status))
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

        Collections.reverse(upcomingEventDTOs); // Đảo ngược thứ tự

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

    @Override
    public String sentRequestForApproval(int eventId) throws MessagingException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        if (event.getStatus() != EEventStatus.DRAFT && event.getStatus() != EEventStatus.PENDING
                && event.getStatus() != EEventStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_EVENT_STATUS);
        }

        event.setStatus(EEventStatus.PENDING);
        eventRepository.save(event);
        Account manager = accountRepository.findManagerWithLeastVerifications()
                .orElseThrow(() -> new AppException(ErrorCode.MANAGER_NOT_FOUND));
        List<Contract> contractList = contractRepository.findContractsByEvent_EventId(eventId);
        for (Contract contract : contractList) {
            if (contract.getStatus().equals(EContractStatus.APPROVED)) {
                throw new AppException(ErrorCode.EVENT_ALREADY_APPROVED);
            }
        }
        Contract contract = new Contract();
        contract.setEvent(event);
        contract.setCompany(event.getCompany());
        contract.setAccount(manager);
        contract.setTotalAmount(0);
        contract.setCommission("0");
        contract.setContractType("STANDARD");
        contract.setContractName("Hợp đồng cho sự kiện " + event.getEventName());
        contract.setStatus(EContractStatus.PENDING);
        contractRepository.save(contract);
        String fullName = event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName();
        emailService.sendEventApprovalRequest(manager.getEmail(), event.getEventName(), fullName);

        Notification notification = new Notification();
        notification.setMessage("Có sự kiện mới cần duyệt");
        notification.setAccount(manager);
        notification.setRead(false);
        notification.setCreatedDate(LocalDateTime.now());
        notification.setReadDate(null);

        notificationRepository.saveAndFlush(notification);
        messagingTemplate.convertAndSendToUser(manager.getUserName(),"/specific/messages", "Có sự kiện mới cần duyệt");


        return "Yêu cầu đã được gửi";

    }

    @Override
    public List<EventForConsumerResponse> getEventsForConsumerByStatusScheduled() {
        List<Event> events = eventRepository.findEventsByStatus(EEventStatus.SCHEDULED);
        if (events.isEmpty()) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }

        List<EventForConsumerResponse> responses = events.stream()
                .map(event -> new EventForConsumerResponse(
                        event.getBannerURL(),
                        event.getEventId(),
                        event.getLogoURL()
                ))
                .collect(Collectors.toList());

        Collections.reverse(responses); // Đảo ngược danh sách

        return responses;
    }


    @Override
    public EventDetailForConsumer getEventDetailForConsumer(int eventId) {
        Event event = eventRepository.findEventByEventId(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // Dùng ModelMapper để map sang DTO
        List<EventActivityDTO> eventActivityDTOList = event.getEventActivities().stream()
                .map(activity -> modelMapper.map(activity, EventActivityDTO.class))
                .collect(Collectors.toList());

        // Ánh xạ từ EventActivityDTO sang EventActivityResponse
        List<EventActivityResponse> eventActivityResponseList = modelMapper.map(eventActivityDTOList, new TypeToken<List<EventActivityResponse>>() {}.getType());

        // Lấy công ty của sự kiện
        Company company = event.getCompany();

        // Lấy giá vé thấp nhất
        double minPrice = ticketRepository.findMinTicketByEvent_EventId(eventId)
                .map(Ticket::getPrice)
                .orElse(0.0);

        // Duyệt qua từng EventActivityResponse để gán Ticket vào từng sự kiện
        for (EventActivityResponse activityResponse : eventActivityResponseList) {
            // Lấy danh sách TicketMapping liên quan đến EventActivity
            List<TicketMapping> ticketMappingList = ticketMappingRepository.findTicketMappingsByEventActivity_EventActivityId(activityResponse.getEventActivityId());

            // Nếu không có TicketMapping, tiếp tục với việc lấy vé từ các Ticket
            List<TicketDTO> ticketDTOS = new ArrayList<>();

            if (!ticketMappingList.isEmpty()) {
                // Lấy danh sách Ticket từ TicketMapping
                for (TicketMapping ticketMapping : ticketMappingList) {
                    Optional<Ticket> ticketOpt = ticketRepository.findById(ticketMapping.getTicket().getTicketId());
                    ticketOpt.ifPresent(ticket -> ticketDTOS.add(modelMapper.map(ticket, TicketDTO.class)));
                }
            } else {
                // Nếu không có TicketMapping, lấy Ticket trực tiếp từ EventActivity hoặc Zone
                // Bạn có thể thêm logic để lấy vé từ Zone hoặc Seat nếu cần
                // Trong trường hợp này, tôi chỉ lấy Ticket mặc định nếu không có TicketMapping
                List<Ticket> tickets = ticketRepository.findTicketsByEvent_EventId(eventId);
                ticketDTOS.addAll(tickets.stream()
                        .map(ticket -> modelMapper.map(ticket, TicketDTO.class))
                        .collect(Collectors.toList()));
            }
            activityResponse.setSoldOut(ticketMappingService.checkTicketMappingExist(activityResponse.getEventActivityId(), ticketDTOS.get(0).getTicketId()));

            // Gán danh sách TicketDTO vào EventActivityResponse
            activityResponse.setTickets(ticketDTOS);
        }



        // Kiểm tra xem sự kiện có seat map không
        boolean isHaveSeatMap = seatMapRepository.findSeatMapByEvent_EventId(eventId).isPresent();
        int eventCatetoryId = event.getCategory() != null ? event.getCategory().getEventCategoryId() : 0;
        return new EventDetailForConsumer(
                event.getEventId(),
                event.getEventName(),
                event.getLocation(),
                event.getLocationName(),
                event.getLogoURL(),
                event.getBannerURL(),
                company != null ? company.getLogoURL() : null,  // URL logo của công ty
                company != null ? company.getCompanyName() : null, // Tên công ty
                company != null ? company.getDescription() : null, // Mô tả công ty
                event.getStatus().name(),
                event.getTypeEvent(),
                event.getDescription(),
                event.getCategory().getCategoryName(),
                eventCatetoryId,
                eventActivityResponseList,
                isHaveSeatMap,
                minPrice
        );
    }

    @Override
    public boolean countView(int eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        event.setCountView(event.getCountView() + 1);
        eventRepository.save(event);
        return true;
    }

    @Override
    public List<EventForConsumerResponse> getEventsForConsumerForWeekend() {
        List<Event> events = eventRepository.findScheduledEvents().stream()
                .filter(event -> event.getEventActivities().stream()
                        .anyMatch(eventActivity -> appUtils.isWeekend(eventActivity.getDateEvent())))
                .filter(event -> event.getStatus() == EEventStatus.SCHEDULED)
                .toList();

        if (events.isEmpty()) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }

        List<EventForConsumerResponse> responses = events.stream()
                .map(event -> new EventForConsumerResponse(
                        event.getBannerURL(),
                        event.getEventId(),
                        event.getLogoURL()
                ))
                .collect(Collectors.toList());

        Collections.reverse(responses); // Đảo ngược danh sách

        return responses;
    }


    @Override
    public List<EventForConsumerResponse> getEventsForConsumerInMonth(int month) {
        List<Event> events = eventRepository.findEventsByStatus(EEventStatus.SCHEDULED).stream()
                .filter(event -> event.getEventActivities().stream()
                        .anyMatch(eventActivity -> eventActivity.getDateEvent().getMonthValue() == month))
                .toList();

        if (events.isEmpty()) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }

        List<EventForConsumerResponse> responses = events.stream()
                .map(event -> new EventForConsumerResponse(
                        event.getBannerURL(),
                        event.getEventId(),
                        event.getLogoURL()
                ))
                .collect(Collectors.toList());

        Collections.reverse(responses); // Đảo ngược danh sách

        return responses;
    }


    @Override
    public List<EventDetailForConsumer> getEventByStartDateAndEndDateAndEventTypeAndEventName(
            String startDate, String endDate, String eventType, String eventName, List<String> eventCategories, Double minPrice, Double maxPrice) {

        // Chuyển đổi startDate và endDate từ String sang LocalDate
        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : null;
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : null;

        // Lấy danh sách sự kiện từ repository với các bộ lọc
        List<Event> events = eventRepository.findEventsByFilter(start, end, eventType, eventName, eventCategories, minPrice, maxPrice);

        // Chuyển đổi danh sách sự kiện thành danh sách EventDetailForConsumer
        List<EventDetailForConsumer> eventDetails = events.stream()
                .map(event -> {
                    Company company = event.getCompany();
                    boolean isHaveSeatMap = event.getSeatMap() != null;
                    List<EventActivityDTO> eventActivityDTOList = event.getEventActivities().stream()
                            .map(activity -> modelMapper.map(activity, EventActivityDTO.class))
                            .collect(Collectors.toList());

                    List<EventActivityResponse> eventActivityResponseList = modelMapper.map(eventActivityDTOList, new TypeToken<List<EventActivityResponse>>() {}.getType());

                    // Lấy giá ticket thấp nhất của sự kiện
                    double minEventPrice = ticketRepository.findMinTicketByEvent_EventId(event.getEventId())
                            .map(Ticket::getPrice)
                            .orElse(0.0);
                    int eventCategoryId = event.getCategory() != null ? event.getCategory().getEventCategoryId() : 0;

                    return new EventDetailForConsumer(
                            event.getEventId(),
                            event.getEventName(),
                            event.getLocation(),
                            event.getLocationName(),
                            event.getLogoURL(),
                            event.getBannerURL(),
                            company != null ? company.getLogoURL() : null,
                            company != null ? company.getCompanyName() : null,
                            company != null ? company.getDescription() : null,
                            event.getStatus().name(),
                            event.getTypeEvent(),
                            event.getDescription(),
                            event.getCategory() != null ? event.getCategory().getCategoryName() : null,
                            eventCategoryId,
                            eventActivityResponseList,
                            isHaveSeatMap,
                            minEventPrice
                    );
                })
                .collect(Collectors.toList());

        return eventDetails;
    }

    @Override
    public List<EventDashboardResponse> getEventDashboardByCompanyId(int companyId) {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        var account = accountRepository.findAccountByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_CREATE_COMPANY));

        if (company.getStatus() != ECompanyStatus.ACTIVE) {
            throw new AppException(ErrorCode.COMPANY_NOT_ACTIVE);
        }

        if (company.getCompanyId() != companyId) {
            throw new AppException(ErrorCode.INVALID_COMPANY);
        }

        List<Event> events = eventRepository.findEventsByCompany_CompanyId(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        List<EventDashboardResponse> eventDashboardResponses = new ArrayList<>();

        for (Event event : events) {
            EventDashboardResponse response = new EventDashboardResponse();
            response.setEventId(event.getEventId());
            response.setEventName(event.getEventName());
            response.setDescription(event.getDescription());
            response.setLocation(event.getLocation());
            response.setLocationName(event.getLocationName());
            response.setLogoURL(event.getLogoURL());
            response.setBannerURL(event.getBannerURL());
            response.setStatus(event.getStatus().name());
            response.setCountView(event.getCountView());
            response.setTypeEvent(event.getTypeEvent());
            response.setStartDate(String.valueOf(event.getStartDate()));
            response.setEndDate(String.valueOf(event.getEndDate()));
            response.setEventCategory(event.getCategory() != null ? event.getCategory().getCategoryName() : null);
            response.setHaveSeatMap(event.getSeatMap() != null);

                        // Dùng ModelMapper để map sang DTO
            List<EventActivityDTO> eventActivityDTOList = event.getEventActivities().stream()
                    .map(activity -> modelMapper.map(activity, EventActivityDTO.class))
                    .collect(Collectors.toList());

            // Ánh xạ từ EventActivityDTO sang EventActivityResponse
            List<EventActivityResponse> eventActivityResponseList = modelMapper.map(eventActivityDTOList, new TypeToken<List<EventActivityResponse>>() {}.getType());

            for (EventActivityResponse activityResponse : eventActivityResponseList) {
                // Lấy danh sách TicketMapping liên quan đến EventActivity
                List<TicketMapping> ticketMappingList = ticketMappingRepository.findTicketMappingsByEventActivity_EventActivityId(activityResponse.getEventActivityId());

                // Nếu không có TicketMapping, tiếp tục với việc lấy vé từ các Ticket
                List<TicketDTO> ticketDTOS = new ArrayList<>();

                if (!ticketMappingList.isEmpty()) {
                    // Lấy danh sách Ticket từ TicketMapping
                    for (TicketMapping ticketMapping : ticketMappingList) {
                        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketMapping.getTicket().getTicketId());
                        ticketOpt.ifPresent(ticket -> ticketDTOS.add(modelMapper.map(ticket, TicketDTO.class)));
                    }
                } else {
                    // Nếu không có TicketMapping, lấy Ticket trực tiếp từ EventActivity hoặc Zone
                    // Bạn có thể thêm logic để lấy vé từ Zone hoặc Seat nếu cần
                    // Trong trường hợp này, tôi chỉ lấy Ticket mặc định nếu không có TicketMapping
                    List<Ticket> tickets = ticketRepository.findTicketsByEvent_EventId(event.getEventId());
                    ticketDTOS.addAll(tickets.stream()
                            .map(ticket -> modelMapper.map(ticket, TicketDTO.class))
                            .collect(Collectors.toList()));
                }

                // Gán danh sách TicketDTO vào EventActivityResponse
                activityResponse.setTickets(ticketDTOS);
            }
            // Gán vào response
            response.setEventActivityDTOList(eventActivityResponseList);

            // Tính tổng số vé bán được & tổng doanh thu
            Integer totalTicketSold = ticketPurchaseRepository.getTotalTicketsSoldByEventId(event.getEventId());
            Double totalRevenue = ticketPurchaseRepository.getTotalPriceByEventId(event.getEventId());

            response.setCountTicketSold(totalTicketSold);
            response.setTotalRevenue(totalRevenue);

            eventDashboardResponses.add(response);
        }

        return eventDashboardResponses;
    }

    @Override
    public boolean appoveEvent(int eventId, EEventStatus status) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        var event = eventRepository.findEventByEventIdAndOrganizer_UserName(eventId, name)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        if (event.getStatus() != EEventStatus.PENDING) {
            throw new AppException(ErrorCode.EVENT_NOT_PENDING_APPROVAL);
        }
        event.setStatus(status);
        eventRepository.save(event);
        return true;
    }


}






